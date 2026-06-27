import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { DoctorAvailabilityService } from '../../shared/services/doctor-availability.service';
import { DoctorAvailabilityPage } from './doctor-availability.page';

describe('DoctorAvailabilityPage', () => {
  let fixture: ComponentFixture<DoctorAvailabilityPage>;
  let component: DoctorAvailabilityPage;
  let availabilityService: {
    list: ReturnType<typeof vi.fn>;
    add: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    remove: ReturnType<typeof vi.fn>;
  };

  const slots = [
    { id: 1, dayOfWeek: 1, startTime: '09:00:00', endTime: '17:00:00', isActive: true },
    { id: 2, dayOfWeek: 3, startTime: '14:00:00', endTime: '18:00:00', isActive: true }
  ];

  beforeEach(async () => {
    availabilityService = {
      list: vi.fn().mockReturnValue(of(slots)),
      add: vi.fn(),
      update: vi.fn(),
      remove: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [DoctorAvailabilityPage],
      providers: [
        provideRouter([]),
        { provide: DoctorAvailabilityService, useValue: availabilityService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorAvailabilityPage);
    component = fixture.componentInstance;
  });

  it('should load availability on init', () => {
    fixture.detectChanges();

    expect(availabilityService.list).toHaveBeenCalled();
    expect(component.slots()).toEqual(slots);
    expect(component.loading()).toBeFalsy();
  });

  it('should display slots grouped by day', () => {
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    const dayHeaders = compiled.querySelectorAll('.day-group h4');
    expect(dayHeaders.length).toBe(2);
    expect(dayHeaders[0].textContent).toContain('Monday');
    expect(dayHeaders[1].textContent).toContain('Wednesday');
  });

  it('should show empty state when no slots exist', () => {
    availabilityService.list.mockReturnValue(of([]));
    fixture.detectChanges();

    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.state-empty')).toBeTruthy();
  });

  it('should show error state when list fails', () => {
    availabilityService.list.mockReturnValue(throwError(() => new Error('Network error')));
    fixture.detectChanges();

    expect(component.error()).toBeTruthy();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('.state-error')).toBeTruthy();
  });

  it('should add a new slot and refresh the list', () => {
    availabilityService.list.mockReturnValue(of(slots));
    fixture.detectChanges();

    const newSlot = { id: 3, dayOfWeek: 5, startTime: '10:00:00', endTime: '14:00:00', isActive: true };
    availabilityService.add.mockReturnValue(of(newSlot));

    component.availabilityForm.setValue({ dayOfWeek: 5, startTime: '10:00', endTime: '14:00' });
    component.submitForm();

    expect(availabilityService.add).toHaveBeenCalledWith({
      dayOfWeek: 5,
      startTime: '10:00:00',
      endTime: '14:00:00'
    });
    expect(component.slots()).toContainEqual(newSlot);
    expect(component.successMessage()).toBeTruthy();
  });

  it('should remove a slot and update the list', () => {
    availabilityService.list.mockReturnValue(of(slots));
    fixture.detectChanges();

    const deactivated = { ...slots[0], isActive: false };
    availabilityService.remove.mockReturnValue(of(deactivated));

    component.confirmRemove(slots[0]);

    expect(availabilityService.remove).toHaveBeenCalledWith(1);
    expect(component.slots().length).toBe(1);
    expect(component.slots()[0].id).toBe(2);
  });

  it('should switch to edit mode when edit button is clicked', () => {
    availabilityService.list.mockReturnValue(of(slots));
    fixture.detectChanges();

    component.startEdit(slots[0]);

    expect(component.editingSlot()).toEqual(slots[0]);
    expect(component.availabilityForm.getRawValue().dayOfWeek).toBe(1);
    expect(component.availabilityForm.getRawValue().startTime).toBe('09:00');
    expect(component.availabilityForm.getRawValue().endTime).toBe('17:00');
  });

  it('should cancel edit and reset the form', () => {
    component.startEdit(slots[0]);
    component.cancelEdit();

    expect(component.editingSlot()).toBeNull();
    expect(component.availabilityForm.getRawValue().dayOfWeek).toBe(1);
    expect(component.availabilityForm.getRawValue().startTime).toBe('09:00');
  });

  it('should not submit when form is invalid', () => {
    availabilityService.list.mockReturnValue(of(slots));
    fixture.detectChanges();

    component.availabilityForm.setValue({ dayOfWeek: 0, startTime: '', endTime: '' });
    component.submitForm();

    expect(availabilityService.add).not.toHaveBeenCalled();
    expect(availabilityService.update).not.toHaveBeenCalled();
  });
});
