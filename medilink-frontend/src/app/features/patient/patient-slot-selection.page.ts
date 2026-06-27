import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
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
  readonly doctorName = signal('');
  readonly doctorSpecialty = signal('');
  readonly doctorAddress = signal('');
  readonly doctorDuration = signal<number | null>(null);

  readonly selectedDate = signal('');
  readonly slots = signal<Slot[]>([]);
  readonly selectedSlot = signal<Slot | null>(null);
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly hasLoaded = signal(false);

  readonly minDate = new Date().toISOString().split('T')[0];

  readonly hasDoctorContext = computed(() => !!this.doctorName() || this.doctorId() > 0);

  readonly morningSlots = computed(() =>
    this.slots().filter((s) => {
      const hour = parseInt(s.startTime.split(':')[0], 10);
      return hour < 12;
    })
  );

  readonly afternoonSlots = computed(() =>
    this.slots().filter((s) => {
      const hour = parseInt(s.startTime.split(':')[0], 10);
      return hour >= 12;
    })
  );

  ngOnInit(): void {
    this.route.params.subscribe((params) => {
      const id = Number(params['doctorId']);
      if (id > 0) {
        this.doctorId.set(id);
      }
    });

    this.route.queryParams.subscribe((qp) => {
      if (qp['name']) this.doctorName.set(qp['name']);
      if (qp['specialty']) this.doctorSpecialty.set(qp['specialty']);
      if (qp['address']) this.doctorAddress.set(qp['address']);
      if (qp['duration']) this.doctorDuration.set(Number(qp['duration']));
    });
  }

  loadSlots(): void {
    const date = this.selectedDate();
    if (!date) return;

    this.isLoading.set(true);
    this.hasLoaded.set(true);
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
    if (this.selectedSlot()?.startTime === slot.startTime) {
      this.selectedSlot.set(null);
    } else {
      this.selectedSlot.set(slot);
    }
  }

  proceedToBooking(): void {
    const slot = this.selectedSlot();
    if (!slot) return;

    const qp: Record<string, string | number> = {
      doctorId: this.doctorId(),
      date: this.selectedDate(),
      startTime: slot.startTime,
      endTime: slot.endTime
    };
    if (this.doctorName()) qp['name'] = this.doctorName();
    if (this.doctorSpecialty()) qp['specialty'] = this.doctorSpecialty();
    if (this.doctorDuration() !== null) qp['duration'] = this.doctorDuration()!;

    this.router.navigate(['/patient/book'], { queryParams: qp });
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
      if (err['status'] === 401) return 'Your session has expired. Please sign in again.';
    }
    return 'Unable to load available slots. Please try again later.';
  }
}
