import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpecialtyManagementService } from './specialty-management.service';

describe('SpecialtyManagementService', () => {
  let service: SpecialtyManagementService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SpecialtyManagementService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SpecialtyManagementService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should list all specialties through the admin endpoint contract', () => {
    const specialties = [
      { id: 1, name: 'Cardiology', description: 'Heart care', status: 'ACTIVE' as const }
    ];

    service.listAll().subscribe((result) => {
      expect(result).toEqual(specialties);
    });

    const request = httpTestingController.expectOne('/v1/specialties?activeOnly=false');
    expect(request.request.method).toBe('GET');
    request.flush(apiResponse(specialties));
  });

  it('should create and update specialties through the backend contract', () => {
    const body = { name: 'Dermatology', description: 'Skin care' };
    const response = { id: 2, name: 'Dermatology', description: 'Skin care', status: 'ACTIVE' as const };

    service.create(body).subscribe((specialty) => {
      expect(specialty).toEqual(response);
    });
    const createRequest = httpTestingController.expectOne('/v1/specialties');
    expect(createRequest.request.method).toBe('POST');
    expect(createRequest.request.body).toEqual(body);
    createRequest.flush(apiResponse(response));

    service.update(2, body).subscribe((specialty) => {
      expect(specialty).toEqual(response);
    });
    const updateRequest = httpTestingController.expectOne('/v1/specialties/2');
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual(body);
    updateRequest.flush(apiResponse(response));
  });

  it('should activate and deactivate specialties through patch endpoints', () => {
    const active = { id: 3, name: 'Pediatrics', description: null, status: 'ACTIVE' as const };
    const inactive = { ...active, status: 'INACTIVE' as const };

    service.activate(3).subscribe((specialty) => {
      expect(specialty).toEqual(active);
    });
    const activateRequest = httpTestingController.expectOne('/v1/specialties/3/activate');
    expect(activateRequest.request.method).toBe('PATCH');
    activateRequest.flush(apiResponse(active));

    service.deactivate(3).subscribe((specialty) => {
      expect(specialty).toEqual(inactive);
    });
    const deactivateRequest = httpTestingController.expectOne('/v1/specialties/3/deactivate');
    expect(deactivateRequest.request.method).toBe('PATCH');
    deactivateRequest.flush(apiResponse(inactive));
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
