import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Slot } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './patient-slot-selection.page.html',
  styleUrl: './patient-slot-selection.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientSlotSelectionPage implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly appointmentService = inject(AppointmentService);

  readonly doctorId = signal<number>(0);
  readonly selectedDate = signal('');
  readonly slots = signal<Slot[]>([]);
  readonly selectedSlot = signal<Slot | null>(null);
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');

  readonly minDate = new Date().toISOString().split('T')[0];

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      const id = Number(params['doctorId']);
      if (id > 0) {
        this.doctorId.set(id);
      }
    });
  }

  loadSlots(): void {
    const date = this.selectedDate();
    if (!date) return;

    this.isLoading.set(true);
    this.errorMessage.set('');
    this.slots.set([]);
    this.selectedSlot.set(null);

    this.appointmentService.getDoctorSlots(this.doctorId(), date).subscribe({
      next: (slots) => {
        this.slots.set(slots);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isLoading.set(false);
      }
    });
  }

  selectSlot(slot: Slot): void {
    this.selectedSlot.set(slot);
  }

  proceedToBooking(): void {
    const slot = this.selectedSlot();
    if (!slot) return;

    this.router.navigate(['/patient/book'], {
      queryParams: {
        doctorId: this.doctorId(),
        date: this.selectedDate(),
        startTime: slot.startTime,
        endTime: slot.endTime
      }
    });
  }

  formatTime(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  }

  formatDate(date: string): string {
    if (!date) return '';
    const parsed = new Date(date + 'T00:00:00');
    return parsed.toLocaleDateString('en-US', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null) {
      const err = error as Record<string, unknown>;
      if (err['status'] === 403) return 'You do not have permission to view available slots.';
      if (err['status'] === 401) return 'Your session has expired. Please log in again.';
    }
    return 'Unable to load available slots. Please try again.';
  }
}
