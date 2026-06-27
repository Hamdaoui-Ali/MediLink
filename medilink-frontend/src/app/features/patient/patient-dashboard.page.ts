import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Appointment } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';
import { AuthService } from '../../shared/services/auth.service';

@Component({
  standalone: true,
  imports: [RouterLink],
  templateUrl: './patient-dashboard.page.html',
  styleUrl: './patient-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientDashboardPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly appointmentService = inject(AppointmentService);

  readonly userName = computed(() => this.authService.currentUser()?.fullName ?? '');
  readonly upcomingAppointments = signal<Appointment[]>([]);
  readonly isLoading = signal(false);
  readonly loadError = signal(false);
  readonly hasLoaded = signal(false);

  readonly nextAppointment = computed(() => this.upcomingAppointments()[0] ?? null);

  ngOnInit(): void {
    this.loadUpcomingAppointments();
  }

  loadUpcomingAppointments(): void {
    this.isLoading.set(true);
    this.loadError.set(false);

    this.appointmentService.listPatientAppointments('upcoming').subscribe({
      next: (appointments) => {
        this.upcomingAppointments.set(appointments);
        this.isLoading.set(false);
        this.hasLoaded.set(true);
      },
      error: () => {
        this.loadError.set(true);
        this.isLoading.set(false);
        this.hasLoaded.set(true);
      }
    });
  }

  formatDate(date: string): string {
    if (!date) return '';
    const parsed = new Date(date + 'T00:00:00');
    return parsed.toLocaleDateString('en-US', { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' });
  }

  formatTime(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  }

  statusLabel(status: string): string {
    const labels: Record<string, string> = {
      CONFIRMED: 'Confirmed', CANCELLED: 'Cancelled',
      COMPLETED: 'Completed', MISSED: 'Missed', RESCHEDULED: 'Rescheduled'
    };
    return labels[status] ?? status;
  }
}
