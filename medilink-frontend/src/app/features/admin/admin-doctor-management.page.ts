import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AdminDoctor, AdminDoctorCreateRequest, AdminDoctorUpdateRequest } from '../../shared/models/admin-doctor.model';
import { Specialty } from '../../shared/models/specialty.model';
import { AdminDoctorService } from '../../shared/services/admin-doctor.service';
import { SpecialtyManagementService } from '../../shared/services/specialty-management.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-doctor-management.page.html',
  styleUrl: './admin-doctor-management.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDoctorManagementPage implements OnInit {
  private readonly doctorService = inject(AdminDoctorService);
  private readonly specialtyService = inject(SpecialtyManagementService);
  private readonly formBuilder = inject(FormBuilder);

  readonly doctors = signal<AdminDoctor[]>([]);
  readonly specialties = signal<Specialty[]>([]);
  readonly selectedDoctor = signal<AdminDoctor | null>(null);
  readonly detailDoctor = signal<AdminDoctor | null>(null);
  readonly searchTerm = signal('');
  readonly statusFilter = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  readonly specialtyFilter = signal<number>(0);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly isResettingPassword = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly resetPasswordMessage = signal('');
  readonly resetPasswordError = signal('');

  readonly activeSpecialties = computed(() =>
    this.specialties().filter((specialty) => specialty.status === 'ACTIVE')
  );
  readonly filteredDoctors = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const status = this.statusFilter();
    const specialtyId = this.specialtyFilter();

    return this.doctors().filter((doctor) => {
      const matchesTerm = !term
        || doctor.fullName.toLowerCase().includes(term)
        || doctor.email.toLowerCase().includes(term)
        || (doctor.phoneNumber ?? '').toLowerCase().includes(term)
        || doctor.specialtyName.toLowerCase().includes(term);
      const matchesStatus = status === 'ALL' || doctor.status === status;
      const matchesSpecialty = specialtyId === 0 || doctor.specialtyId === specialtyId;
      return matchesTerm && matchesStatus && matchesSpecialty;
    });
  });
  readonly activeDoctorCount = computed(() => this.doctors().filter((doctor) => doctor.status === 'ACTIVE').length);
  readonly inactiveDoctorCount = computed(() => this.doctors().filter((doctor) => doctor.status !== 'ACTIVE').length);

  readonly doctorForm = this.formBuilder.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.minLength(8)]],
    phoneNumber: [''],
    specialtyId: [0, [Validators.required, Validators.min(1)]],
    biography: [''],
    consultationDurationMinutes: [30, [Validators.required, Validators.min(5)]],
    clinicAddress: ['']
  });
  readonly passwordForm = this.formBuilder.nonNullable.group({
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.doctorService.listDoctors().subscribe({
      next: (doctors) => {
        this.doctors.set(doctors);
        this.syncDetailDoctor();
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load doctors.'));
        this.isLoading.set(false);
      }
    });

    this.specialtyService.listAll().subscribe({
      next: (specialties) => this.specialties.set(specialties),
      error: () => undefined
    });
  }

  saveDoctor(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.doctorForm.markAllAsTouched();

    const selected = this.selectedDoctor();
    if (!selected && !this.doctorForm.controls.password.value.trim()) {
      this.doctorForm.controls.password.setErrors({ required: true });
    }

    if (this.doctorForm.invalid || this.isSaving()) {
      return;
    }

    this.isSaving.set(true);
    const save$ = selected
      ? this.doctorService.updateDoctor(selected.id, this.toUpdateRequest())
      : this.doctorService.createDoctor(this.toCreateRequest());

    save$.subscribe({
      next: (doctor) => {
        this.upsertDoctor(doctor);
        this.resetForm();
        this.successMessage.set(selected ? 'Doctor updated.' : 'Doctor account created.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to save doctor.'));
        this.isSaving.set(false);
      }
    });
  }

  editDoctor(doctor: AdminDoctor): void {
    this.selectedDoctor.set(doctor);
    this.errorMessage.set('');
    this.successMessage.set('');
    this.doctorForm.setValue({
      fullName: doctor.fullName,
      email: doctor.email,
      password: '',
      phoneNumber: doctor.phoneNumber ?? '',
      specialtyId: doctor.specialtyId,
      biography: doctor.biography ?? '',
      consultationDurationMinutes: doctor.consultationDurationMinutes,
      clinicAddress: doctor.clinicAddress ?? ''
    });
  }

  selectDoctor(doctor: AdminDoctor): void {
    this.detailDoctor.set(doctor);
    this.resetPasswordMessage.set('');
    this.resetPasswordError.set('');
    this.passwordForm.reset({ password: '' });
  }

  activateDoctor(doctor: AdminDoctor): void {
    this.toggleDoctorStatus(doctor, true);
  }

  deactivateDoctor(doctor: AdminDoctor): void {
    this.toggleDoctorStatus(doctor, false);
  }

  resetForm(): void {
    this.selectedDoctor.set(null);
    this.doctorForm.reset({
      fullName: '',
      email: '',
      password: '',
      phoneNumber: '',
      specialtyId: 0,
      biography: '',
      consultationDurationMinutes: 30,
      clinicAddress: ''
    });
    this.doctorForm.markAsUntouched();
  }

  clearFilters(): void {
    this.searchTerm.set('');
    this.statusFilter.set('ALL');
    this.specialtyFilter.set(0);
  }

  setStatusFilter(value: string): void {
    this.statusFilter.set(value === 'ACTIVE' || value === 'INACTIVE' ? value : 'ALL');
  }

  setSpecialtyFilter(value: string): void {
    this.specialtyFilter.set(Number(value) || 0);
  }

  resetPassword(): void {
    const doctor = this.detailDoctor();
    if (!doctor || this.isResettingPassword()) return;

    this.passwordForm.markAllAsTouched();
    this.resetPasswordMessage.set('');
    this.resetPasswordError.set('');
    if (this.passwordForm.invalid) return;

    this.isResettingPassword.set(true);
    this.doctorService.resetPassword(doctor.id, {
      password: this.passwordForm.controls.password.value.trim()
    }).subscribe({
      next: (updated) => {
        this.upsertDoctor(updated);
        this.detailDoctor.set(updated);
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

  hasError(controlName: keyof typeof this.doctorForm.controls): boolean {
    const control = this.doctorForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  hasPasswordError(): boolean {
    const control = this.passwordForm.controls.password;
    return control.invalid && (control.dirty || control.touched);
  }

  private toggleDoctorStatus(doctor: AdminDoctor, active: boolean): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    const toggle$ = active
      ? this.doctorService.activateDoctor(doctor.id)
      : this.doctorService.deactivateDoctor(doctor.id);

    toggle$.subscribe({
      next: (updated) => {
        this.upsertDoctor(updated);
        if (this.detailDoctor()?.id === updated.id) {
          this.detailDoctor.set(updated);
        }
        this.successMessage.set(active ? 'Doctor activated.' : 'Doctor deactivated.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to update doctor status.'));
      }
    });
  }

  private upsertDoctor(doctor: AdminDoctor): void {
    this.doctors.update((current) => {
      const withoutDoctor = current.filter((item) => item.id !== doctor.id);
      return [...withoutDoctor, doctor].sort((a, b) => a.fullName.localeCompare(b.fullName));
    });
  }

  private syncDetailDoctor(): void {
    const selected = this.detailDoctor();
    if (!selected) return;
    this.detailDoctor.set(this.doctors().find((doctor) => doctor.id === selected.id) ?? null);
  }

  private toCreateRequest(): AdminDoctorCreateRequest {
    const value = this.doctorForm.getRawValue();
    return {
      ...this.toUpdateRequest(),
      password: value.password.trim()
    };
  }

  private toUpdateRequest(): AdminDoctorUpdateRequest {
    const value = this.doctorForm.getRawValue();
    return {
      fullName: value.fullName.trim(),
      email: value.email.trim().toLowerCase(),
      phoneNumber: value.phoneNumber.trim() || null,
      specialtyId: Number(value.specialtyId),
      biography: value.biography.trim() || null,
      consultationDurationMinutes: Number(value.consultationDurationMinutes),
      clinicAddress: value.clinicAddress.trim() || null
    };
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 400) {
        return 'Check the doctor details and required fields.';
      }
      if (error.status === 401) {
        return 'Your session has expired. Please sign in again.';
      }
      if (error.status === 403) {
        return 'You do not have permission to manage doctors.';
      }
      if (error.status === 409) {
        return 'A user with this email already exists.';
      }
    }
    return fallback;
  }
}
