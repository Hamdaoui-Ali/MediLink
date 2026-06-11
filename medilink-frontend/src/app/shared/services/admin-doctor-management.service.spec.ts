import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AdminDoctorManagementService } from './admin-doctor-management.service';

describe('AdminDoctorManagementService', () => {
  let service: AdminDoctorManagementService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AdminDoctorManagementService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AdminDoctorManagementService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should fetch doctors from the admin endpoint', () => {
    service.list().subscribe((doctors) => {
      expect(doctors).toEqual([
        expect.objectContaining({
          id: 1,
          fullName: 'Dr Care'
        })
      ]);
    });

    const request = httpTestingController.expectOne('/v1/admin/doctors');
    expect(request.request.method).toBe('GET');
    request.flush({
      success: true,
      data: [
        {
          id: 1,
          fullName: 'Dr Care',
          email: 'doctor@example.com',
          specialtyId: 2,
          specialtyName: 'Cardiology',
          consultationDurationMinutes: 30,
          status: 'ACTIVE'
        }
      ]
    });
  });

  it('should create, update, activate, and deactivate doctors through the contract endpoints', () => {
    const requestBody = {
      fullName: 'Dr Care',
      email: 'doctor@example.com',
      password: 'Doctor@123',
      specialtyId: 2,
      consultationDurationMinutes: 30
    };

    service.create(requestBody).subscribe();
    let request = httpTestingController.expectOne('/v1/admin/doctors');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(requestBody);
    request.flush({ success: true, data: { id: 1, ...requestBody, specialtyName: 'Cardiology', status: 'ACTIVE' } });

    service.update(1, requestBody).subscribe();
    request = httpTestingController.expectOne('/v1/admin/doctors/1');
    expect(request.request.method).toBe('PUT');
    request.flush({ success: true, data: { id: 1, ...requestBody, specialtyName: 'Cardiology', status: 'ACTIVE' } });

    service.activate(1).subscribe();
    request = httpTestingController.expectOne('/v1/admin/doctors/1/activate');
    expect(request.request.method).toBe('PATCH');
    request.flush({ success: true, data: { id: 1, ...requestBody, specialtyName: 'Cardiology', status: 'ACTIVE' } });

    service.deactivate(1).subscribe();
    request = httpTestingController.expectOne('/v1/admin/doctors/1/deactivate');
    expect(request.request.method).toBe('PATCH');
    request.flush({ success: true, data: { id: 1, ...requestBody, specialtyName: 'Cardiology', status: 'INACTIVE' } });
  });
});
