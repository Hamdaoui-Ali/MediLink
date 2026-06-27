import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { DoctorSummary } from '../../shared/models/doctor-profile.model';
import { DoctorProfileService } from '../../shared/services/doctor-profile.service';

@Component({
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './patient-doctor-search.page.html',
  styleUrl: './patient-doctor-search.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientDoctorSearchPage {
  private readonly doctorProfileService = inject(DoctorProfileService);

  readonly doctors = signal<DoctorSummary[]>([]);
  readonly searchName = signal('');
  readonly isLoading = signal(false);
  readonly hasSearched = signal(false);
  readonly errorMessage = signal('');

  search(): void {
    const name = this.searchName().trim();
    if (!name) return;

    this.hasSearched.set(true);
    this.isLoading.set(true);
    this.errorMessage.set('');
    this.doctors.set([]);

    this.doctorProfileService.searchDoctors(undefined, name).subscribe({
      next: (doctors) => {
        this.doctors.set(doctors);
        this.isLoading.set(false);
      },
      error: (error) => {
        this.errorMessage.set(this.getErrorMessage(error));
        this.isLoading.set(false);
      }
    });
  }

  resetSearch(): void {
    this.searchName.set('');
    this.doctors.set([]);
    this.hasSearched.set(false);
    this.errorMessage.set('');
  }

  private getErrorMessage(error: unknown): string {
    if (typeof error === 'object' && error !== null) {
      const err = error as Record<string, unknown>;
      if (err['status'] === 403) return 'You do not have permission to search for doctors.';
      if (err['status'] === 401) return 'Your session has expired. Please sign in again.';
    }
    return 'Unable to load doctors. Please try again later.';
  }
}
