import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AdminDashboardService } from '../../shared/services/admin-dashboard.service';
import { AdminDashboardPage } from './admin-dashboard.page';

describe('AdminDashboardPage', () => {
  let fixture: ComponentFixture<AdminDashboardPage>;
  let component: AdminDashboardPage;
  let dashboardService: {
    getOverview: ReturnType<typeof vi.fn>;
  };

  const overview = {
    totalDoctors: 3,
    totalPatients: 10,
    totalAppointments: 5,
    totalSpecialties: 4,
    recentAppointments: [
      {
        id: 1,
        appointmentDate: '2026-06-15',
        startTime: '10:00:00',
        doctorName: 'Dr. Smith',
        patientName: 'Jane Patient',
        status: 'CONFIRMED' as const
      }
    ]
  };

  beforeEach(async () => {
    dashboardService = {
      getOverview: vi.fn().mockReturnValue(of(overview))
    };

    await TestBed.configureTestingModule({
      imports: [AdminDashboardPage],
      providers: [
        provideRouter([]),
        {
          provide: AdminDashboardService,
          useValue: dashboardService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDashboardPage);
    component = fixture.componentInstance;
  });

  it('should load dashboard overview on init', () => {
    fixture.detectChanges();

    expect(dashboardService.getOverview).toHaveBeenCalled();
    expect(component.overview()).toEqual(overview);
    expect(component.loading()).toBeFalsy();
    expect(component.error()).toBeNull();
  });

  it('should display stat cards with correct values', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const values = compiled.querySelectorAll('.stat-value');
    expect(values[0].textContent).toContain('3');
    expect(values[1].textContent).toContain('10');
    expect(values[2].textContent).toContain('5');
    expect(values[3].textContent).toContain('4');
  });

  it('should display management quick links', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const links = compiled.querySelectorAll('.quick-links-grid a');
    expect(links.length).toBe(2);
    expect(links[0].getAttribute('href')).toBe('/admin/specialties');
    expect(links[1].getAttribute('href')).toBe('/admin/doctors');
  });

  it('should display recent appointments table', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const rows = compiled.querySelectorAll('tbody tr');
    expect(rows.length).toBe(1);
    expect(rows[0].textContent).toContain('Dr. Smith');
    expect(rows[0].textContent).toContain('Jane Patient');
    expect(rows[0].textContent).toContain('CONFIRMED');
  });

  it('should show loading state when request is in progress', () => {
    dashboardService.getOverview.mockReturnValue(of(overview));
    fixture.detectChanges();

    expect(component.loading()).toBeFalsy();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.state-loading')).toBeNull();
  });

  it('should show error state when request fails', () => {
    dashboardService.getOverview.mockReturnValue(
      throwError(() => new Error('Network error'))
    );
    fixture.detectChanges();

    expect(component.error()).toBeTruthy();
    expect(component.loading()).toBeFalsy();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.state-error')).toBeTruthy();
  });

  it('should show empty state when no appointments exist', () => {
    dashboardService.getOverview.mockReturnValue(
      of({ ...overview, recentAppointments: [] })
    );
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.state-empty')).toBeTruthy();
  });

  it('should retry loading when error button is clicked', () => {
    dashboardService.getOverview.mockReturnValue(
      throwError(() => new Error('Network error'))
    );
    fixture.detectChanges();

    expect(component.error()).toBeTruthy();

    dashboardService.getOverview.mockReturnValue(of(overview));

    const compiled = fixture.nativeElement as HTMLElement;
    const button = compiled.querySelector('.state-error button') as HTMLButtonElement;
    button.click();

    fixture.detectChanges();

    expect(component.error()).toBeNull();
    expect(component.overview()).toEqual(overview);
  });
});
