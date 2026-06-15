import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="role-card">
      <h2>Patient workspace</h2>
      <p>Prepared for doctor discovery, slot selection, booking flow, and appointment history.</p>
      <nav class="dashboard-links">
        <a routerLink="/patient/book" class="dashboard-link">Book an Appointment</a>
      </nav>
    </section>
  `,
  styles: `
    .role-card {
      padding: 1.5rem;
      border-radius: 20px;
      background: #f3f7ff;
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
export class PatientDashboardPage {}
