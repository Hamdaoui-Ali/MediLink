import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <section class="role-card">
      <h2>Admin workspace</h2>
      <p>Prepared for doctor account management, specialty management, and system monitoring.</p>
    </section>
  `,
  styles: `
    .role-card {
      padding: 1.5rem;
      border-radius: 20px;
      background: #fffaf1;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class AdminDashboardPage {}
