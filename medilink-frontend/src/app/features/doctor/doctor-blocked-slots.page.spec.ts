import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { BlockedSlot } from '../../shared/models/blocked-slot.model';
import { BlockedSlotService } from '../../shared/services/blocked-slot.service';
import { DoctorBlockedSlotsPage } from './doctor-blocked-slots.page';

describe('DoctorBlockedSlotsPage', () => {
  let fixture: ComponentFixture<DoctorBlockedSlotsPage>;
  let component: DoctorBlockedSlotsPage;
  let blockedSlotService: {
    listDoctorBlockedSlots: ReturnType<typeof vi.fn>;
    createBlockedSlot: ReturnType<typeof vi.fn>;
    updateBlockedSlot: ReturnType<typeof vi.fn>;
    deleteBlockedSlot: ReturnType<typeof vi.fn>;
  };

  const mockSlots: BlockedSlot[] = [
    {
      id: 1,
      doctorId: 1,
      blockDate: '2026-06-20',
      startTime: '10:00:00',
      endTime: '12:00:00',
      reason: 'Vacation'
    },
    {
      id: 2,
      doctorId: 1,
      blockDate: '2026-07-15',
      startTime: '14:00:00',
      endTime: '16:00:00',
      reason: null
    }
  ];

  beforeEach(async () => {
    blockedSlotService = {
      listDoctorBlockedSlots: vi.fn().mockReturnValue(of(mockSlots)),
      createBlockedSlot: vi.fn(),
      updateBlockedSlot: vi.fn(),
      deleteBlockedSlot: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [DoctorBlockedSlotsPage],
      providers: [
        provideRouter([]),
        {
          provide: BlockedSlotService,
          useValue: blockedSlotService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorBlockedSlotsPage);
    component = fixture.componentInstance;
  });

  it('should load blocked slots on init', () => {
    fixture.detectChanges();

    expect(blockedSlotService.listDoctorBlockedSlots).toHaveBeenCalled();
    expect(component.blockedSlots().length).toBe(2);
    expect(component.isLoading()).toBeFalsy();
  });

  it('should show error message when loading fails', () => {
    blockedSlotService.listDoctorBlockedSlots.mockReturnValue(
      throwError(() => ({ status: 403 }))
    );

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('You do not have permission to manage blocked slots.');
    expect(component.isLoading()).toBeFalsy();
  });

  it('should create a blocked slot and add it to the list', () => {
    fixture.detectChanges();
    const created: BlockedSlot = {
      id: 3,
      doctorId: 1,
      blockDate: '2026-08-01',
      startTime: '09:00:00',
      endTime: '17:00:00',
      reason: 'Conference'
    };
    blockedSlotService.createBlockedSlot.mockReturnValue(of(created));

    component.slotForm.setValue({
      blockDate: '2026-08-01',
      startTime: '09:00',
      endTime: '17:00',
      reason: 'Conference'
    });
    component.saveBlockedSlot();

    expect(blockedSlotService.createBlockedSlot).toHaveBeenCalledWith({
      blockDate: '2026-08-01',
      startTime: '09:00',
      endTime: '17:00',
      reason: 'Conference'
    });
    expect(component.blockedSlots().length).toBe(3);
    expect(component.successMessage()).toBe('Blocked slot created.');
  });

  it('should not submit invalid form', () => {
    component.slotForm.setValue({
      blockDate: '',
      startTime: '',
      endTime: '',
      reason: ''
    });
    component.saveBlockedSlot();

    expect(blockedSlotService.createBlockedSlot).not.toHaveBeenCalled();
  });

  it('should edit a blocked slot and update it in the list', () => {
    fixture.detectChanges();
    const updated: BlockedSlot = {
      ...mockSlots[0],
      startTime: '11:00:00',
      endTime: '13:00:00',
      reason: 'Updated vacation'
    };
    blockedSlotService.updateBlockedSlot.mockReturnValue(of(updated));

    component.editBlockedSlot(mockSlots[0]);
    component.slotForm.patchValue({
      startTime: '11:00',
      endTime: '13:00',
      reason: 'Updated vacation'
    });
    component.saveBlockedSlot();

    expect(blockedSlotService.updateBlockedSlot).toHaveBeenCalledWith(1, {
      blockDate: '2026-06-20',
      startTime: '11:00',
      endTime: '13:00',
      reason: 'Updated vacation'
    });
    expect(component.blockedSlots().find((slot) => slot.id === 1)?.reason).toBe('Updated vacation');
    expect(component.selectedSlot()).toBeNull();
    expect(component.successMessage()).toBe('Blocked slot updated.');
  });

  it('should delete a blocked slot and remove it from the list', () => {
    fixture.detectChanges();
    blockedSlotService.deleteBlockedSlot.mockReturnValue(of(undefined));

    component.deleteBlockedSlot(mockSlots[0]);

    expect(blockedSlotService.deleteBlockedSlot).toHaveBeenCalledWith(1);
    expect(component.blockedSlots().length).toBe(1);
    expect(component.successMessage()).toBe('Blocked slot removed.');
  });

  it('should show validation errors for required fields', () => {
    fixture.detectChanges();

    component.slotForm.controls.blockDate.markAsDirty();
    component.slotForm.controls.blockDate.markAsTouched();

    expect(component.hasError('blockDate')).toBe(true);
  });

  it('should format time correctly', () => {
    expect(component.formatTime('10:00:00')).toBe('10:00 AM');
    expect(component.formatTime('14:30:00')).toBe('2:30 PM');
    expect(component.formatTime('')).toBe('');
  });

  it('should format date correctly', () => {
    const formatted = component.formatDate('2026-06-20');
    expect(formatted).toContain('2026');
    expect(formatted).toContain('Jun');
    expect(formatted).toContain('20');
  });

  it('should get min date as today', () => {
    const minDate = component.getMinDate();
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');
    expect(minDate).toBe(`${year}-${month}-${day}`);
  });
});
