import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Appointment } from '../../shared/models/appointment.model';
import { DoctorProfile } from '../../shared/models/doctor-profile.model';
import { AppointmentService } from '../../shared/services/appointment.service';
import { AuthService } from '../../shared/services/auth.service';
import { DoctorProfileService } from '../../shared/services/doctor-profile.service';

@Component({
  standalone: true,
  imports: [RouterLink],
  templateUrl: './doctor-dashboard.page.html',
  styleUrl: './doctor-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorDashboardPage implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly appointmentService = inject(AppointmentService);
  private readonly profileService = inject(DoctorProfileService);

  readonly userName = computed(() => {
    const user = this.authService.currentUser();
    return user?.fullName ?? 'Doctor';
  });

  readonly appointments = signal<Appointment[]>([]);
  readonly profile = signal<DoctorProfile | null>(null);
  readonly isAppointmentsLoading = signal(false);
  readonly isProfileLoading = signal(false);
  readonly appointmentsError = signal(false);
  readonly profileError = signal(false);

  readonly todayAppointments = computed(() => {
    const today = new Date().toISOString().split('T')[0];
    return this.appointments().filter((a) => a.appointmentDate === today);
  });

  readonly upcomingAppointments = computed(() =>
    this.appointments().filter((a) =>
      a.status === 'CONFIRMED' || a.status === 'RESCHEDULED'
    )
  );

  readonly profileIncomplete = computed(() => {
    const p = this.profile();
    if (!p) return false;
    return !p.biography || !p.clinicAddress;
  });

  ngOnInit(): void {
    this.loadAppointments();
    this.loadProfile();
  }

  loadAppointments(): void {
    this.isAppointmentsLoading.set(true);
    this.appointmentsError.set(false);
    this.appointmentService.listDoctorAppointments().subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.isAppointmentsLoading.set(false);
      },
      error: () => {
        this.appointmentsError.set(true);
        this.isAppointmentsLoading.set(false);
      }
    });
  }

  loadProfile(): void {
    this.isProfileLoading.set(true);
    this.profileError.set(false);
    this.profileService.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.isProfileLoading.set(false);
      },
      error: () => {
        this.profileError.set(true);
        this.isProfileLoading.set(false);
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
