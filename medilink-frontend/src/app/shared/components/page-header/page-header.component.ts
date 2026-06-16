import { ChangeDetectionStrategy, Component, input } from '@angular/core';

@Component({
  selector: 'app-page-header',
  standalone: true,
  template: `
    <section class="page-header">
      <p class="page-header__eyebrow">MediLink frontend</p>
      <h2>{{ title() }}</h2>
      <p>{{ subtitle() }}</p>
    </section>
  `,
  styles: `
    .page-header {
      display: grid;
      gap: 0.5rem;
      margin-bottom: 1.5rem;
    }

    .page-header__eyebrow {
      margin: 0;
      text-transform: uppercase;
      letter-spacing: 0.14em;
      font-size: 0.72rem;
      color: #e76f51;
    }

    h2 {
      margin: 0;
      font-size: 1.7rem;
      line-height: 1.1;
    }

    p {
      margin: 0;
      color: #415a77;
    }
  `,
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class PageHeaderComponent {
  readonly title = input.required<string>();
  readonly subtitle = input.required<string>();
}
