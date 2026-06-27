import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DoctorAvailability, DoctorAvailabilityRequest } from '../../shared/models/doctor-availability.model';
import { DoctorAvailabilityService } from '../../shared/services/doctor-availability.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-availability.page.html',
  styleUrl: './doctor-availability.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorAvailabilityPage implements OnInit {
  private readonly availabilityService = inject(DoctorAvailabilityService);
  private readonly formBuilder = inject(FormBuilder);

  readonly availability = signal<DoctorAvailability[]>([]);
  readonly selectedAvailability = signal<DoctorAvailability | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly days = [
    { value: 1, label: 'Monday' },
    { value: 2, label: 'Tuesday' },
    { value: 3, label: 'Wednesday' },
    { value: 4, label: 'Thursday' },
    { value: 5, label: 'Friday' },
    { value: 6, label: 'Saturday' },
    { value: 7, label: 'Sunday' }
  ];

  readonly availabilityForm = this.formBuilder.nonNullable.group({
    dayOfWeek: [1, [Validators.required, Validators.min(1), Validators.max(7)]],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required]
  });

  readonly groupedAvailability = computed(() =>
    this.days.map((day) => ({
      ...day,
      ranges: this.availability().filter((range) => range.dayOfWeek === day.value)
    }))
  );

  ngOnInit(): void {
    this.loadAvailability();
  }

  loadAvailability(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.availabilityService.listAvailability().subscribe({
      next: (availability) => {
        this.availability.set(availability);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load weekly availability.'));
        this.isLoading.set(false);
      }
    });
  }

  saveAvailability(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.availabilityForm.markAllAsTouched();

    if (this.availabilityForm.invalid || this.isSaving()) {
      return;
    }

    const selected = this.selectedAvailability();
    const request = this.toRequest();
    const save$ = selected
      ? this.availabilityService.updateAvailability(selected.id, request)
      : this.availabilityService.createAvailability(request);

    this.isSaving.set(true);
    save$.subscribe({
      next: (saved) => {
        this.availability.update((current) => {
          const withoutSaved = current.filter((range) => range.id !== saved.id);
          return [...withoutSaved, saved].sort(this.sortAvailability);
        });
        this.resetForm();
        this.successMessage.set(selected ? 'Availability updated.' : 'Availability added.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to save availability.'));
        this.isSaving.set(false);
      }
    });
  }

  editAvailability(range: DoctorAvailability): void {
    this.selectedAvailability.set(range);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.availabilityForm.setValue({
      dayOfWeek: range.dayOfWeek,
      startTime: this.trimSeconds(range.startTime),
      endTime: this.trimSeconds(range.endTime)
    });
  }

  deleteAvailability(range: DoctorAvailability): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    this.availabilityService.deleteAvailability(range.id).subscribe({
      next: () => {
        this.availability.update((current) => current.filter((item) => item.id !== range.id));
        if (this.selectedAvailability()?.id === range.id) {
          this.resetForm();
        }
        this.successMessage.set('Availability removed.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to remove availability.'));
      }
    });
  }

  resetForm(): void {
    this.selectedAvailability.set(null);
    this.availabilityForm.reset({
      dayOfWeek: 1,
      startTime: '',
      endTime: ''
    });
    this.availabilityForm.markAsUntouched();
  }

  hasError(controlName: keyof typeof this.availabilityForm.controls): boolean {
    const control = this.availabilityForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  dayLabel(dayOfWeek: number): string {
    return this.days.find((day) => day.value === dayOfWeek)?.label ?? `Day ${dayOfWeek}`;
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

  private toRequest(): DoctorAvailabilityRequest {
    const formValue = this.availabilityForm.getRawValue();
    return {
      dayOfWeek: Number(formValue.dayOfWeek),
      startTime: formValue.startTime,
      endTime: formValue.endTime
    };
  }

  private trimSeconds(time: string): string {
    return time ? time.slice(0, 5) : '';
  }

  private sortAvailability(a: DoctorAvailability, b: DoctorAvailability): number {
    if (a.dayOfWeek !== b.dayOfWeek) {
      return a.dayOfWeek - b.dayOfWeek;
    }
    return a.startTime.localeCompare(b.startTime);
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 400) {
        return 'Check the day and time range. Availability cannot overlap existing ranges.';
      }
      if (error.status === 401) {
        return 'Your session has expired. Please sign in again.';
      }
      if (error.status === 403) {
        return 'You do not have permission to manage doctor availability.';
      }
    }
    return fallback;
  }
}
