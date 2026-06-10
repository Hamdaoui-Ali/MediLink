import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { PatientRegistrationService } from './patient-registration.service';

describe('PatientRegistrationService', () => {
  let service: PatientRegistrationService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PatientRegistrationService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(PatientRegistrationService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should post patient registration and unwrap the API response', () => {
    const request = {
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      password: 'Patient@123',
      phoneNumber: '+15551234567',
      dateOfBirth: null,
      gender: null,
      address: null
    };
    const response = {
      userId: 1,
      patientId: 2,
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      phoneNumber: '+15551234567',
      role: 'PATIENT' as const,
      accountStatus: 'ACTIVE' as const
    };

    service.register(request).subscribe((registeredPatient) => {
      expect(registeredPatient).toEqual(response);
    });

    const httpRequest = httpTestingController.expectOne('/v1/patients/register');
    expect(httpRequest.request.method).toBe('POST');
    expect(httpRequest.request.body).toEqual(request);
    httpRequest.flush({
      success: true,
      data: response,
      error: null,
      timestamp: new Date().toISOString()
    });
  });
});
