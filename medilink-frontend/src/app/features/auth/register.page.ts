import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { PatientGender, PatientRegistrationRequest } from '../../shared/models/patient-registration.model';
import { PatientRegistrationService } from '../../shared/services/patient-registration.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.page.html',
  styleUrl: './register.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class RegisterPage {
  private readonly formBuilder = inject(FormBuilder);
  private readonly patientRegistrationService = inject(PatientRegistrationService);
  private readonly router = inject(Router);

  readonly genders: PatientGender[] = ['FEMALE', 'MALE', 'OTHER', 'UNSPECIFIED'];
  readonly isSubmitting = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly submitLabel = computed(() => this.isSubmitting() ? 'Creating account...' : 'Create account');

  readonly registrationForm = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(160)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
    phoneNumber: ['', [Validators.required, Validators.maxLength(40)]],
    dateOfBirth: [''],
    gender: [''],
    address: ['', [Validators.maxLength(500)]]
  });

  submit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.registrationForm.markAllAsTouched();

    if (this.registrationForm.invalid || this.isSubmitting()) {
      return;
    }

    this.isSubmitting.set(true);

    this.patientRegistrationService.register(this.toRequest()).subscribe({
      next: (registeredPatient) => {
        this.successMessage.set('Account created. Redirecting to login...');
        this.isSubmitting.set(false);
        this.router.navigate(['/login'], {
          queryParams: {
            registered: 'true',
            email: registeredPatient.email
          }
        });
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isSubmitting.set(false);
      }
    });
  }

  hasError(controlName: keyof typeof this.registrationForm.controls): boolean {
    const control = this.registrationForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  toRequest(): PatientRegistrationRequest {
    const formValue = this.registrationForm.getRawValue();

    return {
      fullName: formValue.fullName.trim(),
      email: formValue.email.trim().toLowerCase(),
      password: formValue.password,
      phoneNumber: formValue.phoneNumber.trim(),
      dateOfBirth: formValue.dateOfBirth || null,
      gender: (formValue.gender as PatientGender) || null,
      address: formValue.address.trim() || null
    };
  }

  getErrorMessage(error: unknown): string {
    if (this.isDuplicateEmailError(error)) {
      return 'An account already exists for this email.';
    }

    return 'Registration failed. Check the form and try again.';
  }

  private isDuplicateEmailError(error: unknown): boolean {
    return typeof error === 'object'
      && error !== null
      && 'status' in error
      && error.status === 409;
  }
}
