import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Specialty, SpecialtyRequest, SpecialtyStatus } from '../../shared/models/specialty.model';
import { SpecialtyManagementService } from '../../shared/services/specialty-management.service';

@Component({
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-specialty-management.page.html',
  styleUrl: './admin-specialty-management.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminSpecialtyManagementPage implements OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly specialtyManagementService = inject(SpecialtyManagementService);

  readonly specialties = signal<Specialty[]>([]);
  readonly selectedSpecialty = signal<Specialty | null>(null);
  readonly isLoading = signal(false);
  readonly isSaving = signal(false);
  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly searchQuery = signal('');
  readonly statusFilter = signal<'ALL' | SpecialtyStatus>('ALL');
  readonly confirmDeactivateId = signal<number | null>(null);

  readonly isEditing = computed(() => this.selectedSpecialty() !== null);
  readonly submitLabel = computed(() => this.isSaving()
    ? 'Saving...'
    : this.isEditing() ? 'Save changes' : 'Create specialty');

  readonly filteredSpecialties = computed(() => {
    let list = this.specialties();
    const query = this.searchQuery().trim().toLowerCase();
    if (query) {
      list = list.filter((s) =>
        s.name.toLowerCase().includes(query) ||
        (s.description ?? '').toLowerCase().includes(query)
      );
    }
    const sf = this.statusFilter();
    if (sf !== 'ALL') {
      list = list.filter((s) => s.status === sf);
    }
    return list;
  });

  readonly specialtyForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(120)]],
    description: ['', [Validators.maxLength(500)]]
  });

  ngOnInit(): void {
    this.loadSpecialties();
  }

  loadSpecialties(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.specialtyManagementService.listAll().subscribe({
      next: (specialties) => {
        this.specialties.set(specialties);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to load specialties.'));
        this.isLoading.set(false);
      }
    });
  }

  startCreate(): void {
    this.selectedSpecialty.set(null);
    this.specialtyForm.reset({ name: '', description: '' });
    this.errorMessage.set('');
    this.successMessage.set('');
    this.confirmDeactivateId.set(null);
  }

  startEdit(specialty: Specialty): void {
    this.selectedSpecialty.set(specialty);
    this.specialtyForm.setValue({
      name: specialty.name,
      description: specialty.description ?? ''
    });
    this.errorMessage.set('');
    this.successMessage.set('');
    this.confirmDeactivateId.set(null);
  }

  submit(): void {
    this.errorMessage.set('');
    this.successMessage.set('');
    this.specialtyForm.markAllAsTouched();

    if (this.specialtyForm.invalid || this.isSaving()) return;

    this.isSaving.set(true);
    const selectedSpecialty = this.selectedSpecialty();
    const request = this.toRequest();
    const operation = selectedSpecialty
      ? this.specialtyManagementService.update(selectedSpecialty.id, request)
      : this.specialtyManagementService.create(request);

    operation.subscribe({
      next: (specialty) => {
        this.upsertSpecialty(specialty);
        this.selectedSpecialty.set(specialty);
        this.specialtyForm.setValue({ name: specialty.name, description: specialty.description ?? '' });
        this.successMessage.set(selectedSpecialty ? 'Specialty updated.' : 'Specialty created.');
        this.isSaving.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to save specialty.'));
        this.isSaving.set(false);
      }
    });
  }

  activate(specialty: Specialty): void {
    this.updateStatus(specialty, true);
  }

  confirmDeactivate(specialty: Specialty): void {
    this.confirmDeactivateId.set(specialty.id);
  }

  cancelDeactivate(): void {
    this.confirmDeactivateId.set(null);
  }

  deactivate(specialty: Specialty): void {
    this.confirmDeactivateId.set(null);
    this.updateStatus(specialty, false);
  }

  hasError(controlName: keyof typeof this.specialtyForm.controls): boolean {
    const control = this.specialtyForm.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }

  setStatusFilter(filter: 'ALL' | SpecialtyStatus): void {
    this.statusFilter.set(filter);
  }

  statusLabel(status: SpecialtyStatus): string {
    return status === 'ACTIVE' ? 'Active' : 'Inactive';
  }

  statusClass(status: SpecialtyStatus): string {
    return status === 'ACTIVE' ? 'badge-success' : 'badge-muted';
  }

  toRequest(): SpecialtyRequest {
    const formValue = this.specialtyForm.getRawValue();
    return {
      name: formValue.name.trim(),
      description: formValue.description.trim() || null
    };
  }

  private updateStatus(specialty: Specialty, shouldActivate: boolean): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    const operation = shouldActivate
      ? this.specialtyManagementService.activate(specialty.id)
      : this.specialtyManagementService.deactivate(specialty.id);

    operation.subscribe({
      next: (updatedSpecialty) => {
        this.upsertSpecialty(updatedSpecialty);
        if (this.selectedSpecialty()?.id === updatedSpecialty.id) {
          this.selectedSpecialty.set(updatedSpecialty);
        }
        this.successMessage.set(shouldActivate ? 'Specialty activated.' : 'Specialty deactivated.');
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error, 'Unable to update specialty status.'));
      }
    });
  }

  private upsertSpecialty(specialty: Specialty): void {
    const nextSpecialties = this.specialties().filter((item) => item.id !== specialty.id);
    this.specialties.set([...nextSpecialties, specialty].sort((left, right) => left.name.localeCompare(right.name)));
  }

  private getErrorMessage(error: unknown, fallback: string): string {
    if (typeof error === 'object' && error !== null && 'status' in error) {
      if (error.status === 403) return 'You do not have permission to manage specialties.';
      if (error.status === 409) return 'A specialty with this name already exists.';
    }
    return fallback;
  }
}
