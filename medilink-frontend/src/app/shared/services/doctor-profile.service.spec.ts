import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { DoctorProfileService } from './doctor-profile.service';
import { DoctorProfile } from '../models/doctor-profile.model';

describe('DoctorProfileService', () => {
  let service: DoctorProfileService;
  let httpTesting: HttpTestingController;

  const mockProfile: DoctorProfile = {
    id: 1,
    userId: 5,
    fullName: 'Dr. Test',
    email: 'dr.test@medilink.local',
    phoneNumber: '+15551234567',
    accountStatus: 'ACTIVE',
    specialtyName: 'Cardiology',
    biography: 'Experienced cardiologist',
    consultationDurationMinutes: 30,
    clinicAddress: '123 Heart Lane',
    status: 'ACTIVE'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DoctorProfileService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DoctorProfileService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should get doctor profile', () => {
    service.getProfile().subscribe((profile) => {
      expect(profile.fullName).toBe('Dr. Test');
      expect(profile.specialtyName).toBe('Cardiology');
    });

    const req = httpTesting.expectOne('/v1/doctor/profile');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: mockProfile });
  });

  it('should update doctor profile', () => {
    const request = {
      biography: 'New bio',
      clinicAddress: 'New address',
      phoneNumber: '+15559999999',
      consultationDurationMinutes: 45
    };

    service.updateProfile(request).subscribe((profile) => {
      expect(profile.biography).toBe('New bio');
    });

    const req = httpTesting.expectOne('/v1/doctor/profile');
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(request);
    req.flush({ success: true, data: { ...mockProfile, ...request } });
  });
});
