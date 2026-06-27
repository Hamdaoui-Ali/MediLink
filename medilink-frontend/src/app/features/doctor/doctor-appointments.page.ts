import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import {
  Appointment,
  AppointmentStatus,
  ALL_STATUSES,
  STATUS_TRANSITIONS,
  isTerminalStatus
} from '../../shared/models/appointment.model';
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

  readonly selectedAppointment = signal<Appointment | null>(null);
  readonly patientHistory = signal<Appointment[]>([]);
  readonly isLoadingHistory = signal(false);
  readonly historyError = signal('');
  readonly notesDraft = signal('');
  readonly isSavingNotes = signal(false);
  readonly isUpdatingStatus = signal(false);
  readonly detailMessage = signal('');
  readonly detailError = signal('');

  readonly validTransitions = computed(() => {
    const selected = this.selectedAppointment();
    if (!selected) return [];
    return STATUS_TRANSITIONS[selected.status] ?? [];
  });

  readonly isTerminal = computed(() => {
    const selected = this.selectedAppointment();
    if (!selected) return false;
    return isTerminalStatus(selected.status);
  });

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

  selectAppointment(appointment: Appointment): void {
    this.selectedAppointment.set(appointment);
    this.notesDraft.set(appointment.doctorNotes ?? '');
    this.patientHistory.set([]);
    this.historyError.set('');
    this.detailMessage.set('');
    this.detailError.set('');
    this.loadPatientHistory(appointment.patientId);
  }

  clearSelection(): void {
    this.selectedAppointment.set(null);
    this.patientHistory.set([]);
    this.historyError.set('');
  }

  saveNotes(): void {
    const appointment = this.selectedAppointment();
    if (!appointment) return;

    this.isSavingNotes.set(true);
    this.detailError.set('');
    this.detailMessage.set('');

    this.appointmentService.updateNotes(appointment.id, this.notesDraft()).subscribe({
      next: (updated) => {
        this.updateAppointmentInList(updated);
        this.selectedAppointment.set(updated);
        this.detailMessage.set('Notes saved.');
        this.isSavingNotes.set(false);
      },
      error: (error) => {
        this.detailError.set(this.getErrorMessage(error, 'Unable to save notes.'));
        this.isSavingNotes.set(false);
      }
    });
  }

  updateStatus(newStatus: AppointmentStatus): void {
    const appointment = this.selectedAppointment();
    if (!appointment) return;

    this.isUpdatingStatus.set(true);
    this.detailError.set('');
    this.detailMessage.set('');

    this.appointmentService.updateStatus(appointment.id, newStatus).subscribe({
      next: (updated) => {
        this.updateAppointmentInList(updated);
        this.selectedAppointment.set(updated);
        this.detailMessage.set(`Appointment marked as ${this.statusLabel(newStatus)}.`);
        this.isUpdatingStatus.set(false);
      },
      error: (error) => {
        this.detailError.set(this.getErrorMessage(error, 'Unable to update appointment status.'));
        this.isUpdatingStatus.set(false);
      }
    });
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
      CONFIRMED: 'badge-info',
      CANCELLED: 'badge-muted',
      COMPLETED: 'badge-success',
      MISSED: 'badge-warning',
      RESCHEDULED: 'badge-purple'
    };
    return classes[status] ?? 'badge-muted';
  }

  formatGender(gender: string | null): string {
    if (!gender) return 'Not recorded';
    return gender
      .toLowerCase()
      .split('_')
      .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
      .join(' ');
  }

  formatAge(dateOfBirth: string | null): string {
    if (!dateOfBirth) return 'Not recorded';
    const birthDate = new Date(dateOfBirth + 'T00:00:00');
    const today = new Date();
    let age = today.getFullYear() - birthDate.getFullYear();
    const monthDelta = today.getMonth() - birthDate.getMonth();
    if (monthDelta < 0 || (monthDelta === 0 && today.getDate() < birthDate.getDate())) {
      age -= 1;
    }
    return `${age} years`;
  }

  contactValue(value: string | null): string {
    return value?.trim() || 'Not recorded';
  }

  historyWithoutSelected(): Appointment[] {
    const selectedId = this.selectedAppointment()?.id;
    return this.patientHistory().filter((appointment) => appointment.id !== selectedId);
  }

  private loadPatientHistory(patientId: number): void {
    this.isLoadingHistory.set(true);
    this.historyError.set('');

    this.appointmentService.listDoctorPatientHistory(patientId).subscribe({
      next: (history) => {
        this.patientHistory.set(history);
        this.isLoadingHistory.set(false);
      },
      error: (error) => {
        this.historyError.set(this.getErrorMessage(error, 'Unable to load patient history.'));
        this.isLoadingHistory.set(false);
      }
    });
  }

  private updateAppointmentInList(updated: Appointment): void {
    this.appointments.set(
      this.appointments().map((a) => (a.id === updated.id ? updated : a))
    );
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 403) {
        return 'You do not have permission to access this page.';
      }
      if (error.status === 401) {
        return 'Your session has expired. Please sign in again.';
      }
      if (error.status === 400) {
        return 'Invalid operation. The status transition may not be allowed.';
      }
    }
    return fallback;
  }
}
