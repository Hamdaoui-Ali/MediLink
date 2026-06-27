import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Appointment } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './patient-book-appointment.page.html',
  styleUrl: './patient-book-appointment.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientBookAppointmentPage implements OnInit {
  private readonly appointmentService = inject(AppointmentService);
  private readonly route = inject(ActivatedRoute);

  readonly doctorId = signal<number | null>(null);
  readonly doctorName = signal('');
  readonly doctorSpecialty = signal('');
  readonly doctorDuration = signal<number | null>(null);
  readonly appointmentDate = signal('');
  readonly startTime = signal('');
  readonly endTime = signal('');
  readonly reason = signal('');
  readonly preFilled = signal(false);

  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  readonly showManualForm = signal(false);

  readonly bookedAppointment = signal<Appointment | null>(null);

  ngOnInit(): void {
    this.route.queryParams.subscribe((params) => {
      const doctorId = Number(params['doctorId']);
      const date = params['date'] as string | undefined;
      const startTime = params['startTime'] as string | undefined;
      const endTime = params['endTime'] as string | undefined;

      if (doctorId > 0 && date && startTime) {
        this.doctorId.set(doctorId);
        this.appointmentDate.set(date);
        this.startTime.set(startTime);
        this.endTime.set(endTime ?? '');
        this.preFilled.set(true);
      }

      if (params['name']) this.doctorName.set(params['name']);
      if (params['specialty']) this.doctorSpecialty.set(params['specialty']);
      if (params['duration']) this.doctorDuration.set(Number(params['duration']));
    });
  }

  book(): void {
    const doctorId = this.doctorId();
    const date = this.appointmentDate();
    const time = this.startTime();
    const reasonText = this.reason();

    if (!doctorId || !date || !time || !reasonText.trim()) {
      this.errorMessage.set('Please fill in all required fields before booking.');
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    const fullTime = time.includes(':') && time.split(':').length === 3 ? time : time + ':00';

    this.appointmentService.bookAppointment({
      doctorId,
      appointmentDate: date,
      startTime: fullTime,
      reason: reasonText.trim()
    }).subscribe({
      next: (appointment) => {
        this.bookedAppointment.set(appointment);
        this.isSubmitting.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isSubmitting.set(false);
      }
    });
  }

  reset(): void {
    this.doctorId.set(null);
    this.doctorName.set('');
    this.doctorSpecialty.set('');
    this.doctorDuration.set(null);
    this.appointmentDate.set('');
    this.startTime.set('');
    this.endTime.set('');
    this.reason.set('');
    this.errorMessage.set('');
    this.bookedAppointment.set(null);
    this.preFilled.set(false);
    this.showManualForm.set(false);
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

  formatTime(time: string): string {
    if (!time) return '';
    const [hours, minutes] = time.split(':');
    const hour = parseInt(hours, 10);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const displayHour = hour % 12 || 12;
    return `${displayHour}:${minutes} ${ampm}`;
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null) {
      const err = error as Record<string, unknown>;
      if (err['status'] === 409) {
        return 'This time slot is no longer available. Please return and choose another time.';
      }
      if (err['status'] === 403) {
        return 'You are not authorized to book this appointment.';
      }
      if (err['status'] === 401) {
        return 'Your session has expired. Please sign in again.';
      }
      if (err['status'] === 400) {
        const body = err['error'] as Record<string, unknown> | undefined;
        const message = (body?.['error'] as Record<string, string>)?.['message'];
        return message ?? 'There was a problem with your booking request. Please check your information.';
      }
    }
    return 'Unable to complete your booking. Please try again later.';
  }
}
