import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Appointment, AppointmentStatus } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './patient-appointments.page.html',
  styleUrl: './patient-appointments.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientAppointmentsPage implements OnInit {
  private readonly appointmentService = inject(AppointmentService);

  readonly appointments = signal<Appointment[]>([]);
  readonly activeTab = signal<'all' | 'upcoming' | 'past'>('all');
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly actionMessage = signal('');
  readonly actionError = signal('');
  readonly isCancelling = signal(false);
  readonly confirmCancelId = signal<number | null>(null);

  ngOnInit(): void {
    this.loadAppointments();
  }

  setTab(tab: 'all' | 'upcoming' | 'past'): void {
    this.activeTab.set(tab);
    this.actionMessage.set('');
    this.actionError.set('');
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const filter = this.activeTab() === 'all' ? undefined : this.activeTab();
    this.appointmentService.listPatientAppointments(filter).subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load appointments.'));
        this.isLoading.set(false);
      }
    });
  }

  confirmCancel(id: number): void {
    this.confirmCancelId.set(id);
  }

  dismissConfirm(): void {
    this.confirmCancelId.set(null);
  }

  cancelAppointment(id: number): void {
    this.isCancelling.set(true);
    this.actionError.set('');
    this.actionMessage.set('');
    this.confirmCancelId.set(null);

    this.appointmentService.cancelAppointment(id).subscribe({
      next: (updated) => {
        this.updateAppointmentInList(updated);
        this.actionMessage.set('Appointment cancelled.');
        this.isCancelling.set(false);
      },
      error: (error) => {
        this.actionError.set(this.getErrorMessage(error, 'Unable to cancel appointment.'));
        this.isCancelling.set(false);
      }
    });
  }

  canCancel(appointment: Appointment): boolean {
    return appointment.status === 'CONFIRMED' || appointment.status === 'RESCHEDULED';
  }

  formatDate(date: string): string {
    if (!date) return '';
    const parsed = new Date(date + 'T00:00:00');
    return parsed.toLocaleDateString('en-US', {
      weekday: 'short', year: 'numeric', month: 'short', day: 'numeric'
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

  statusLabel(status: AppointmentStatus): string {
    const labels: Record<AppointmentStatus, string> = {
      CONFIRMED: 'Confirmed', CANCELLED: 'Cancelled',
      COMPLETED: 'Completed', MISSED: 'Missed', RESCHEDULED: 'Rescheduled'
    };
    return labels[status] ?? status;
  }

  statusClass(status: AppointmentStatus): string {
    const classes: Record<AppointmentStatus, string> = {
      CONFIRMED: 'status-confirmed', CANCELLED: 'status-cancelled',
      COMPLETED: 'status-completed', MISSED: 'status-missed',
      RESCHEDULED: 'status-rescheduled'
    };
    return classes[status] ?? '';
  }

  private updateAppointmentInList(updated: Appointment): void {
    this.appointments.set(this.appointments().map(a => a.id === updated.id ? updated : a));
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null) {
      const err = error as Record<string, unknown>;
      if (err['status'] === 403) return 'You do not have permission.';
      if (err['status'] === 401) return 'Session expired. Please log in again.';
    }
    return fallback;
  }
}
