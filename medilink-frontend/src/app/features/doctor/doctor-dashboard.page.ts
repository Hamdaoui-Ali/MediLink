import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <section class="role-card">
      <h2>Doctor workspace</h2>
      <p>Prepared for schedule management, blocked slots, appointment review, and visit notes.</p>
    </section>
  `,
  styles: `
    .role-card {
      padding: 1.5rem;
      border-radius: 20px;
      background: #f5fbf8;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DoctorDashboardPage {}
