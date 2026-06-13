import { ChangeDetectionStrategy, Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { NgClass } from '@angular/common';
import { DashboardOverview } from '../../shared/models/dashboard-overview.model';
import { AdminDashboardService } from '../../shared/services/admin-dashboard.service';

@Component({
  standalone: true,
  imports: [RouterLink, NgClass],
  templateUrl: './admin-dashboard.page.html',
  styleUrl: './admin-dashboard.page.scss',
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardPage implements OnInit {
  private readonly adminDashboardService = inject(AdminDashboardService);

  readonly overview = signal<DashboardOverview | null>(null);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.loadDashboard();
  }

  loadDashboard(): void {
    this.loading.set(true);
    this.error.set(null);

    this.adminDashboardService.getOverview().subscribe({
      next: (data) => {
        this.overview.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load dashboard data. Please check your connection and try again.');
        this.loading.set(false);
      }
    });
  }
}
