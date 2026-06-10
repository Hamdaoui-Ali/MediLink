import { ChangeDetectionStrategy, Component } from '@angular/core';

@Component({
  standalone: true,
  template: `
    <section class="role-card">
      <h2>Patient workspace</h2>
      <p>Prepared for doctor discovery, slot selection, booking flow, and appointment history.</p>
    </section>
  `,
  styles: `
    .role-card {
      padding: 1.5rem;
      border-radius: 20px;
      background: #f3f7ff;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PatientDashboardPage {}
