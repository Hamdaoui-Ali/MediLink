import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="role-card">
      <h2>Doctor workspace</h2>
      <p>Prepared for schedule management, blocked slots, appointment review, and visit notes.</p>
      <nav class="dashboard-links">
        <a routerLink="/doctor/appointments" class="dashboard-link">View My Appointments</a>
        <a routerLink="/doctor/blocked-slots" class="dashboard-link">Manage Blocked Slots</a>
      </nav>
    </section>
  `,
  styles: `
    .role-card {
      padding: 1.5rem;
      border-radius: 20px;
      background: #f5fbf8;
    }

    .dashboard-links {
      margin-top: 1rem;
      display: flex;
      gap: 0.75rem;
      flex-wrap: wrap;
    }

    .dashboard-link {
      display: inline-block;
      padding: 0.5rem 0.9rem;
      border-radius: 6px;
      background: #1d4ed8;
      color: #ffffff;
      font-weight: 800;
      text-decoration: none;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorDashboardPage {}
