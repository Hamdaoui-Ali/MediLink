import { ChangeDetectionStrategy, Component } from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="admin-dashboard">
      <header>
        <p class="eyebrow">Admin</p>
        <h2>Admin workspace</h2>
      </header>

      <nav aria-label="Admin management">
        <a routerLink="/admin/doctors">
          <span>Doctor management</span>
          <strong>Create, edit, activate, and deactivate doctors</strong>
        </a>
      </nav>
    </section>
  `,
  styles: `
    .admin-dashboard {
      display: grid;
      gap: 1rem;
      padding: 1.5rem;
      border: 1px solid #d9e2ec;
      border-radius: 8px;
      background: #ffffff;
    }

    h2 {
      margin: 0;
      font-size: 1.9rem;
    }

    .eyebrow {
      margin: 0 0 0.25rem;
      color: #2a9d8f;
      font-size: 0.75rem;
      font-weight: 800;
      text-transform: uppercase;
    }

    nav {
      display: grid;
      gap: 0.75rem;
      max-width: 34rem;
    }

    a {
      display: grid;
      gap: 0.25rem;
      padding: 1rem;
      border: 1px solid #cad5df;
      border-radius: 8px;
      color: #14213d;
      text-decoration: none;
    }

    a span {
      font-weight: 800;
    }

    a strong {
      color: #52616f;
      font-size: 0.92rem;
      font-weight: 600;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardPage {}
