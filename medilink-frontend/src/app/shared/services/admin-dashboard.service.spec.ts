import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdminDashboardService } from './admin-dashboard.service';

describe('AdminDashboardService', () => {
  let service: AdminDashboardService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminDashboardService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AdminDashboardService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should fetch dashboard overview through the backend contract', () => {
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

    service.getOverview().subscribe((result) => {
      expect(result).toEqual(overview);
    });

    const request = httpTestingController.expectOne('/v1/admin/dashboard');
    expect(request.request.method).toBe('GET');
    request.flush(apiResponse(overview));
  });

  it('should handle empty dashboard data gracefully', () => {
    const overview = {
      totalDoctors: 0,
      totalPatients: 0,
      totalAppointments: 0,
      totalSpecialties: 0,
      recentAppointments: []
    };

    service.getOverview().subscribe((result) => {
      expect(result).toEqual(overview);
    });

    const request = httpTestingController.expectOne('/v1/admin/dashboard');
    request.flush(apiResponse(overview));
  });

  function apiResponse<T>(data: T) {
    return {
      success: true,
      data,
      error: null,
      timestamp: new Date().toISOString()
    };
  }
});
