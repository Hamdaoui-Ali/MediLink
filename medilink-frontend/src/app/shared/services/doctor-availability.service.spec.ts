import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { DoctorAvailabilityService } from './doctor-availability.service';

describe('DoctorAvailabilityService', () => {
  let service: DoctorAvailabilityService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        DoctorAvailabilityService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(DoctorAvailabilityService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should list availability slots', () => {
    const slots = [
      { id: 1, dayOfWeek: 1, startTime: '09:00:00', endTime: '17:00:00', isActive: true }
    ];

    service.list().subscribe((result) => {
      expect(result).toEqual(slots);
    });

    const request = httpTestingController.expectOne('/v1/doctor/availability');
    expect(request.request.method).toBe('GET');
    request.flush(apiResponse(slots));
  });

  it('should add a new availability slot', () => {
    const body = { dayOfWeek: 3, startTime: '14:00:00', endTime: '18:00:00' };
    const response = { id: 2, ...body, isActive: true };

    service.add(body).subscribe((result) => {
      expect(result).toEqual(response);
    });

    const request = httpTestingController.expectOne('/v1/doctor/availability');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(body);
    request.flush(apiResponse(response));
  });

  it('should update an existing slot', () => {
    const body = { dayOfWeek: 5, startTime: '10:00:00', endTime: '14:00:00' };
    const response = { id: 3, ...body, isActive: true };

    service.update(3, body).subscribe((result) => {
      expect(result).toEqual(response);
    });

    const request = httpTestingController.expectOne('/v1/doctor/availability/3');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(body);
    request.flush(apiResponse(response));
  });

  it('should remove a slot via delete endpoint', () => {
    const response = { id: 4, dayOfWeek: 2, startTime: '09:00:00', endTime: '12:00:00', isActive: false };

    service.remove(4).subscribe((result) => {
      expect(result).toEqual(response);
    });

    const request = httpTestingController.expectOne('/v1/doctor/availability/4');
    expect(request.request.method).toBe('DELETE');
    request.flush(apiResponse(response));
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
