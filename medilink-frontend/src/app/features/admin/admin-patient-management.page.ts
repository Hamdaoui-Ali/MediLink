import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AccountStatus, AdminPatient, AdminPatientCreateRequest, AdminPatientUpdateRequest } from '../../shared/models/admin-patient.model';
import { PatientGender } from '../../shared/models/patient-registration.model';
import { AdminPatientService } from '../../shared/services/admin-patient.service';

const GENDERS: PatientGender[] = ['FEMALE', 'MALE', 'OTHER', 'UNSPECIFIED'];

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-patient-management.page.html',
  styleUrl: './admin-patient-management.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminPatientManagementPage implements OnInit {
  private readonly patientService = inject(AdminPatientService);
  private readonly formBuilder = inject(FormBuilder);

  readonly genders = GENDERS;
  readonly patients = signal<AdminPatient[]>([]);
  readonly selectedPatient = signal<AdminPatient | null>(null);
  readonly detailPatient = signal<AdminPatient | null>(null);
  readonly searchTerm = signal('');
  readonly statusFilter = signal<AccountStatus | 'ALL'>('ALL');
  readonly genderFilter = signal<PatientGender | 'ALL'>('ALL');
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isResettingPassword = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly resetPasswordMessage = signal('');
  readonly resetPasswordError = signal('');

  readonly activePatientCount = computed(() => this.patients().filter((patient) => patient.accountStatus === 'ACTIVE').length);
  readonly inactivePatientCount = computed(() => this.patients().filter((patient) => patient.accountStatus !== 'ACTIVE').length);
  readonly filteredPatients = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();
    const gender = this.genderFilter();

    return this.patients().filter((patient) => {
      const matchesTerm = !term
        || patient.fullName.toLowerCase().includes(term)
        || patient.email.toLowerCase().includes(term)
        || (patient.phoneNumber ?? '').toLowerCase().includes(term)
        || (patient.address ?? '').toLowerCase().includes(term);
      const matchesStatus = status === 'ALL' || patient.accountStatus === status;
      const matchesGender = gender === 'ALL' || patient.gender === gender;
      return matchesTerm && matchesStatus && matchesGender;
    });
  });

  readonly patientForm = this.formBuilder.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
    phoneNumber: [''],
    dateOfBirth: [''],
    gender: ['UNSPECIFIED' as PatientGender, Validators.required],
    address: ['']
  });
  readonly passwordForm = this.formBuilder.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  ngOnInit(): void {
    this.loadPatients();
  }

  loadPatients(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.patientService.listPatients().subscribe({
      next: (patients) => {
        this.patients.set(patients);
        this.syncDetailPatient();
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load patients.'));
        this.isLoading.set(false);
      }
    });
  }

  savePatient(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.patientForm.markAllAsTouched();

    const selected = this.selectedPatient();
    if (!selected && !this.patientForm.controls.password.value.trim()) {
      this.patientForm.controls.password.setErrors({ required: true });
    }

    if (this.patientForm.invalid || this.isSaving()) return;

    this.isSaving.set(true);
    const save$ = selected
      ? this.patientService.updatePatient(selected.id, this.toUpdateRequest())
      : this.patientService.createPatient(this.toCreateRequest());

    save$.subscribe({
      next: (patient) => {
        this.upsertPatient(patient);
        this.detailPatient.set(patient);
        this.resetForm();
        this.successMessage.set(selected ? 'Patient updated.' : 'Patient account created.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to save patient.'));
        this.isSaving.set(false);
      }
    });
  }

  editPatient(patient: AdminPatient): void {
    this.selectedPatient.set(patient);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.patientForm.setValue({
      fullName: patient.fullName,
      email: patient.email,
      password: '',
      phoneNumber: patient.phoneNumber ?? '',
      dateOfBirth: patient.dateOfBirth ?? '',
      gender: patient.gender ?? 'UNSPECIFIED',
      address: patient.address ?? ''
    });
  }

  selectPatient(patient: AdminPatient): void {
    this.detailPatient.set(patient);
    this.resetPasswordMessage.set('');
    this.resetPasswordError.set('');
    this.passwordForm.reset({ password: '' });
  }

  activatePatient(patient: AdminPatient): void {
    this.togglePatientStatus(patient, true);
  }

  deactivatePatient(patient: AdminPatient): void {
    this.togglePatientStatus(patient, false);
  }

  resetForm(): void {
    this.selectedPatient.set(null);
    this.patientForm.reset({
      fullName: '',
      email: '',
      password: '',
      phoneNumber: '',
      dateOfBirth: '',
      gender: 'UNSPECIFIED',
      address: ''
    });
    this.patientForm.markAsUntouched();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('ALL');
    this.genderFilter.set('ALL');
  }

  setStatusFilter(value: string): void {
    if (value === 'ACTIVE' || value === 'INACTIVE' || value === 'DISABLED') {
      this.statusFilter.set(value);
    } else {
      this.statusFilter.set('ALL');
    }
  }

  setGenderFilter(value: string): void {
    this.genderFilter.set(this.genders.includes(value as PatientGender) ? value as PatientGender : 'ALL');
  }

  resetPassword(): void {
    const patient = this.detailPatient();
    if (!patient || this.isResettingPassword()) return;

    this.passwordForm.markAllAsTouched();
    this.resetPasswordMessage.set('');
    this.resetPasswordError.set('');
    if (this.passwordForm.invalid) return;

    this.isResettingPassword.set(true);
    this.patientService.resetPassword(patient.id, {
      password: this.passwordForm.controls.password.value.trim()
    }).subscribe({
      next: (updated) => {
        this.upsertPatient(updated);
        this.detailPatient.set(updated);
        this.passwordForm.reset({ password: '' });
        this.resetPasswordMessage.set('Temporary password updated.');
        this.isResettingPassword.set(false);
      },
      error: (error) => {
        this.resetPasswordError.set(this.getErrorMessage(error, 'Unable to reset password.'));
        this.isResettingPassword.set(false);
      }
    });
  }

  hasError(controlName: keyof typeof this.patientForm.controls): boolean {
    const control = this.patientForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  hasPasswordError(): boolean {
    const control = this.passwordForm.controls.password;
    return control.invalid && (control.dirty || control.touched);
  }

  formatGender(gender: PatientGender): string {
    return gender.toLowerCase().replace('_', ' ').replace(/^\w/, (char) => char.toUpperCase());
  }

  formatDate(date: string | null): string {
    if (!date) return 'Not recorded';
    return new Date(date + 'T00:00:00').toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  }

  private togglePatientStatus(patient: AdminPatient, active: boolean): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    const toggle$ = active
      ? this.patientService.activatePatient(patient.id)
      : this.patientService.deactivatePatient(patient.id);

    toggle$.subscribe({
      next: (updated) => {
        this.upsertPatient(updated);
        if (this.detailPatient()?.id === updated.id) {
          this.detailPatient.set(updated);
        }
        this.successMessage.set(active ? 'Patient activated.' : 'Patient deactivated.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to update patient status.'));
      }
    });
  }

  private upsertPatient(patient: AdminPatient): void {
    this.patients.update((current) => {
      const withoutPatient = current.filter((item) => item.id !== patient.id);
      return [...withoutPatient, patient].sort((a, b) => a.fullName.localeCompare(b.fullName));
    });
  }

  private syncDetailPatient(): void {
    const selected = this.detailPatient();
    if (!selected) return;
    this.detailPatient.set(this.patients().find((patient) => patient.id === selected.id) ?? null);
  }

  private toCreateRequest(): AdminPatientCreateRequest {
    const value = this.patientForm.getRawValue();
    return {
      ...this.toUpdateRequest(),
      password: value.password.trim()
    };
  }

  private toUpdateRequest(): AdminPatientUpdateRequest {
    const value = this.patientForm.getRawValue();
    return {
      fullName: value.fullName.trim(),
      email: value.email.trim().toLowerCase(),
      phoneNumber: value.phoneNumber.trim() || null,
      dateOfBirth: value.dateOfBirth || null,
      gender: value.gender,
      address: value.address.trim() || null
    };
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 400) return 'Check the patient details and required fields.';
      if (error.status === 401) return 'Your session has expired. Please sign in again.';
      if (error.status === 403) return 'You do not have permission to manage patients.';
      if (error.status === 409) return 'A user with this email already exists.';
    }
    return fallback;
  }
}
