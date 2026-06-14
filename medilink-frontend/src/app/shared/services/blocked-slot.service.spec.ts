import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { BlockedSlotService } from './blocked-slot.service';
import { BlockedSlot } from '../models/blocked-slot.model';

describe('BlockedSlotService', () => {
  let service: BlockedSlotService;
  let httpTesting: HttpTestingController;

  const mockSlots: BlockedSlot[] = [
    {
      id: 1,
      doctorId: 1,
      blockDate: '2026-06-20',
      startTime: '10:00:00',
      endTime: '12:00:00',
      reason: 'Vacation'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        BlockedSlotService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(BlockedSlotService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should list doctor blocked slots', () => {
    service.listDoctorBlockedSlots().subscribe((slots) => {
      expect(slots.length).toBe(1);
      expect(slots[0].reason).toBe('Vacation');
    });

    const req = httpTesting.expectOne('/v1/doctor/blocked-slots');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: mockSlots });
  });

  it('should create a blocked slot', () => {
    const request = {
      blockDate: '2026-06-20',
      startTime: '10:00:00',
      endTime: '12:00:00',
      reason: 'Vacation'
    };

    service.createBlockedSlot(request).subscribe((slot) => {
      expect(slot.reason).toBe('Vacation');
    });

    const req = httpTesting.expectOne('/v1/doctor/blocked-slots');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush({ success: true, data: mockSlots[0] });
  });

  it('should delete a blocked slot', () => {
    service.deleteBlockedSlot(1).subscribe((result) => {
      expect(result).toBeUndefined();
    });

    const req = httpTesting.expectOne('/v1/doctor/blocked-slots/1');
    expect(req.request.method).toBe('DELETE');
    req.flush({ success: true, data: null });
  });
});
