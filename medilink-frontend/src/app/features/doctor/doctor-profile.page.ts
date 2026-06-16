import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DoctorProfile } from '../../shared/models/doctor-profile.model';
import { DoctorProfileService } from '../../shared/services/doctor-profile.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-profile.page.html',
  styleUrl: './doctor-profile.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorProfilePage implements OnInit {
  private readonly doctorProfileService = inject(DoctorProfileService);
  private readonly formBuilder = inject(FormBuilder);

  readonly profile = signal<DoctorProfile | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');

  readonly editForm = this.formBuilder.nonNullable.group({
    biography: ['', [Validators.maxLength(2000)]],
    clinicAddress: ['', [Validators.maxLength(500)]],
    phoneNumber: [''],
    consultationDurationMinutes: [30, [Validators.required, Validators.min(5)]]
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.doctorProfileService.getProfile().subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.editForm.setValue({
          biography: profile.biography ?? '',
          clinicAddress: profile.clinicAddress ?? '',
          phoneNumber: profile.phoneNumber ?? '',
          consultationDurationMinutes: profile.consultationDurationMinutes
        });
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load profile.'));
        this.isLoading.set(false);
      }
    });
  }

  saveProfile(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.editForm.markAllAsTouched();

    if (this.editForm.invalid || this.isSaving()) {
      return;
    }

    this.isSaving.set(true);
    const request = this.toRequest();

    this.doctorProfileService.updateProfile(request).subscribe({
      next: (profile) => {
        this.profile.set(profile);
        this.successMessage.set('Profile updated.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to save profile.'));
        this.isSaving.set(false);
      }
    });
  }

  hasError(controlName: keyof typeof this.editForm.controls): boolean {
    const control = this.editForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  private toRequest() {
    const formValue = this.editForm.getRawValue();
    return {
      biography: formValue.biography.trim() || null,
      clinicAddress: formValue.clinicAddress.trim() || null,
      phoneNumber: formValue.phoneNumber.trim() || null,
      consultationDurationMinutes: formValue.consultationDurationMinutes
    };
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
