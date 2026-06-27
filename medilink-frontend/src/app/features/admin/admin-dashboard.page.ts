import { ChangeDetectionStrategy, Component, OnInit, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Specialty } from '../../shared/models/specialty.model';
import { SpecialtyManagementService } from '../../shared/services/specialty-management.service';

interface AdminModule {
  label: string;
  description: string;
  route?: string;
  comingSoon?: boolean;
  icon: string;
}

const ADMIN_MODULES: AdminModule[] = [
  { label: 'Specialties', description: 'Manage medical specialties for doctor profiles and patient search.', route: '/admin/specialties', icon: '&#127973;' },
  { label: 'Doctors', description: 'Create doctor accounts and manage profile status.', route: '/admin/doctors', icon: '&#128104;&#8205;&#9877;&#65039;' },
  { label: 'Patients', description: 'Create patient accounts and manage profile status.', route: '/admin/patients', icon: '&#128101;' },
  { label: 'Appointments', description: 'Oversee all scheduled and completed appointments.', comingSoon: true, icon: '&#128197;' },
  { label: 'Activity Log', description: 'Review platform activity and audit trail.', comingSoon: true, icon: '&#128269;' },
];

@Component({
  standalone: true,
  imports: [RouterLink],
  templateUrl: './admin-dashboard.page.html',
  styleUrl: './admin-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardPage implements OnInit {
  private readonly specialtyService = inject(SpecialtyManagementService);

  readonly specialties = signal<Specialty[]>([]);
  readonly isLoadingStats = signal(false);

  readonly activeSpecialties = computed(() =>
    this.specialties().filter((s) => s.status === 'ACTIVE').length
  );

  readonly inactiveSpecialties = computed(() =>
    this.specialties().filter((s) => s.status === 'INACTIVE').length
  );

  readonly modules = ADMIN_MODULES;

  ngOnInit(): void {
    this.loadStats();
  }

  loadStats(): void {
    this.isLoadingStats.set(true);
    this.specialtyService.listAll().subscribe({
      next: (specialties) => {
        this.specialties.set(specialties);
        this.isLoadingStats.set(false);
      },
      error: () => {
        this.isLoadingStats.set(false);
      }
    });
  }
}
