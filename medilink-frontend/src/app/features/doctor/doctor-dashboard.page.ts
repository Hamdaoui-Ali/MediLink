import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="doctor-dashboard">
      <div>
        <p class="eyebrow">Doctor workspace</p>
        <h2>Your Practice</h2>
      </div>

      <nav>
        <a routerLink="/doctor/availability">
          <span>Weekly Availability</span>
          <strong>Set your recurring schedule</strong>
        </a>
      </nav>
    </section>
  `,
  styles: `
    .doctor-dashboard {
      padding: 1.5rem;
      background: #f6f8fb;
      min-height: 100vh;
    }

    .eyebrow {
      margin: 0 0 0.35rem;
      color: #059669;
      font-size: 0.78rem;
      font-weight: 800;
      text-transform: uppercase;
    }

    h2 {
      margin: 0 0 1rem;
      color: #172033;
    }

    nav {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 0.75rem;
      max-width: 760px;
    }

    a {
      display: grid;
      gap: 0.35rem;
      border: 1px solid #d9e2ec;
      border-radius: 8px;
      padding: 1rem;
      background: #ffffff;
      color: #172033;
      text-decoration: none;
      transition: border-color 0.15s;
    }

    a:hover {
      border-color: #059669;
    }

    span {
      color: #059669;
      font-weight: 800;
    }

    strong {
      font-size: 0.92rem;
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorDashboardPage {}
