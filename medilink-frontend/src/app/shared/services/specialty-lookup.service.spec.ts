import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { SpecialtyLookupService } from './specialty-lookup.service';

describe('SpecialtyLookupService', () => {
  let service: SpecialtyLookupService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        SpecialtyLookupService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(SpecialtyLookupService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should fetch active specialties for doctor assignment', () => {
    service.listActive().subscribe((specialties) => {
      expect(specialties).toEqual([
        {
          id: 2,
          name: 'Cardiology',
          description: 'Heart care',
          status: 'ACTIVE'
        }
      ]);
    });

    const request = httpTestingController.expectOne('/v1/specialties?activeOnly=true');
    expect(request.request.method).toBe('GET');
    request.flush({
      success: true,
      data: [
        {
          id: 2,
          name: 'Cardiology',
          description: 'Heart care',
          status: 'ACTIVE'
        }
      ]
    });
  });
});
