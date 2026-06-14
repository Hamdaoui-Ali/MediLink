import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Appointment, AppointmentStatus, ALL_STATUSES } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './doctor-appointments.page.html',
  styleUrl: './doctor-appointments.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorAppointmentsPage implements OnInit {
  private readonly appointmentService = inject(AppointmentService);

  readonly ALL_STATUSES = ALL_STATUSES;

  readonly appointments = signal<Appointment[]>([]);
  readonly isLoading = signal(false);
  readonly errorMessage = signal('');
  readonly statusFilter = signal<AppointmentStatus | ''>('');
  readonly dateFilter = signal('');

  ngOnInit(): void {
    this.loadAppointments();
  }

  loadAppointments(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    const status = this.statusFilter() || undefined;
    const date = this.dateFilter() || undefined;

    this.appointmentService.listDoctorAppointments(status, date, date).subscribe({
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

  applyFilters(): void {
    this.loadAppointments();
  }

  clearFilters(): void {
    this.statusFilter.set('');
    this.dateFilter.set('');
    this.loadAppointments();
  }

  isInitialLoad(): boolean {
    return this.isLoading() && this.appointments().length === 0;
  }

  formatTime(time: string): string {
    if (!time) {
      return '';
    }
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  }

  formatDate(date: string): string {
    if (!date) {
      return '';
    }
    const parsed = new Date(date + 'T00:00:00');
    return parsed.toLocaleDateString('en-US', {
      weekday: 'short',
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  statusLabel(status: AppointmentStatus): string {
    const labels: Record<AppointmentStatus, string> = {
      CONFIRMED: 'Confirmed',
      CANCELLED: 'Cancelled',
      COMPLETED: 'Completed',
      MISSED: 'Missed',
      RESCHEDULED: 'Rescheduled'
    };
    return labels[status] ?? status;
  }

  statusClass(status: AppointmentStatus): string {
    const classes: Record<AppointmentStatus, string> = {
      CONFIRMED: 'status-confirmed',
      CANCELLED: 'status-cancelled',
      COMPLETED: 'status-completed',
      MISSED: 'status-missed',
      RESCHEDULED: 'status-rescheduled'
    };
    return classes[status] ?? '';
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 403) {
        return 'You do not have permission to access this page.';
      }
      if (error.status === 401) {
        return 'Your session has expired. Please log in again.';
      }
    }
    return fallback;
  }
}
