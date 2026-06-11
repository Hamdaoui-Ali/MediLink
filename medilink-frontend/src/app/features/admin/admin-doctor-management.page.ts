import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { AdminDoctor, AdminDoctorRequest } from '../../shared/models/admin-doctor.model';
import { AdminDoctorManagementService } from '../../shared/services/admin-doctor-management.service';
import { SpecialtyLookupService } from '../../shared/services/specialty-lookup.service';

@Component({
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './admin-doctor-management.page.html',
  styleUrl: './admin-doctor-management.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDoctorManagementPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly doctorService = inject(AdminDoctorManagementService);
  private readonly specialtyService = inject(SpecialtyLookupService);

  readonly doctors = signal<AdminDoctor[]>([]);
  readonly specialties = signal<{ id: number; name: string }[]>([]);
  readonly selectedDoctor = signal<AdminDoctor | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly submitLabel = computed(() => this.selectedDoctor() ? 'Save changes' : 'Create doctor');
  readonly formTitle = computed(() => this.selectedDoctor() ? 'Edit doctor' : 'Create doctor');

  readonly doctorForm = this.formBuilder.nonNullable.group({
    fullName: ['', [Validators.required, Validators.maxLength(160)]],
    email: ['', [Validators.required, Validators.email, Validators.maxLength(190)]],
    password: ['', [Validators.maxLength(72)]],
    phoneNumber: ['', [Validators.maxLength(40)]],
    specialtyId: ['', [Validators.required]],
    biography: ['', [Validators.maxLength(2000)]],
    consultationDurationMinutes: [30, [Validators.required, Validators.min(5), Validators.max(240)]],
    clinicAddress: ['', [Validators.maxLength(500)]]
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    forkJoin({
      doctors: this.doctorService.list(),
      specialties: this.specialtyService.listActive()
    }).subscribe({
      next: ({ doctors, specialties }) => {
        this.doctors.set(doctors);
        this.specialties.set(specialties.map(({ id, name }) => ({ id, name })));
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isLoading.set(false);
      }
    });
  }

  submit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.doctorForm.markAllAsTouched();

    if (this.doctorForm.invalid || this.isSaving()) {
      return;
    }

    if (!this.selectedDoctor() && !this.doctorForm.controls.password.value.trim()) {
      this.errorMessage.set('Password is required when creating a doctor.');
      return;
    }

    this.isSaving.set(true);
    const selectedDoctor = this.selectedDoctor();
    const request = this.toRequest();
    const save$ = selectedDoctor
      ? this.doctorService.update(selectedDoctor.id, request)
      : this.doctorService.create(request);

    save$.subscribe({
      next: (doctor) => {
        this.upsertDoctor(doctor);
        this.successMessage.set(selectedDoctor ? 'Doctor updated.' : 'Doctor created.');
        this.resetForm();
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isSaving.set(false);
      }
    });
  }

  edit(doctor: AdminDoctor): void {
    this.selectedDoctor.set(doctor);
    this.successMessage.set('');
    this.errorMessage.set('');
    this.doctorForm.setValue({
      fullName: doctor.fullName,
      email: doctor.email,
      password: '',
      phoneNumber: doctor.phoneNumber ?? '',
      specialtyId: String(doctor.specialtyId),
      biography: doctor.biography ?? '',
      consultationDurationMinutes: doctor.consultationDurationMinutes,
      clinicAddress: doctor.clinicAddress ?? ''
    });
  }

  resetForm(): void {
    this.selectedDoctor.set(null);
    this.doctorForm.reset({
      fullName: '',
      email: '',
      password: '',
      phoneNumber: '',
      specialtyId: '',
      biography: '',
      consultationDurationMinutes: 30,
      clinicAddress: ''
    });
  }

  activate(doctor: AdminDoctor): void {
    this.changeStatus(doctor, true);
  }

  deactivate(doctor: AdminDoctor): void {
    this.changeStatus(doctor, false);
  }

  hasError(controlName: keyof typeof this.doctorForm.controls): boolean {
    const control = this.doctorForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  toRequest(): AdminDoctorRequest {
    const value = this.doctorForm.getRawValue();
    const password = value.password.trim();

    return {
      fullName: value.fullName.trim(),
      email: value.email.trim().toLowerCase(),
      password: password || undefined,
      phoneNumber: value.phoneNumber.trim() || null,
      specialtyId: Number(value.specialtyId),
      biography: value.biography.trim() || null,
      consultationDurationMinutes: Number(value.consultationDurationMinutes),
      clinicAddress: value.clinicAddress.trim() || null
    };
  }

  private changeStatus(doctor: AdminDoctor, shouldActivate: boolean): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    const request$ = shouldActivate
      ? this.doctorService.activate(doctor.id)
      : this.doctorService.deactivate(doctor.id);

    request$.subscribe({
      next: (updatedDoctor) => {
        this.upsertDoctor(updatedDoctor);
        this.successMessage.set(shouldActivate ? 'Doctor activated.' : 'Doctor deactivated.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
      }
    });
  }

  private upsertDoctor(doctor: AdminDoctor): void {
    const doctors = this.doctors();
    const index = doctors.findIndex((item) => item.id === doctor.id);

    if (index === -1) {
      this.doctors.set([doctor, ...doctors]);
      return;
    }

    this.doctors.set(doctors.map((item) => item.id === doctor.id ? doctor : item));
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 0) {
        return 'Cannot reach the backend. Make sure the API is running.';
      }

      if (error.status === 403) {
        return 'Only admins can manage doctors.';
      }

      if (error.status === 409) {
        return 'A doctor account already exists with this email.';
      }
    }

    return 'Doctor management request failed.';
  }
}
