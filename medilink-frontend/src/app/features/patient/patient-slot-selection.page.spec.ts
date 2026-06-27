import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { PatientSlotSelectionPage } from './patient-slot-selection.page';
import { Slot } from '../../shared/models/appointment.model';

describe('PatientSlotSelectionPage', () => {
  let component: PatientSlotSelectionPage;
  let fixture: ComponentFixture<PatientSlotSelectionPage>;
  let httpMock: HttpTestingController;

  const mockSlots: Slot[] = [
    { startTime: '09:00', endTime: '09:30' },
    { startTime: '09:30', endTime: '10:00' },
    { startTime: '10:00', endTime: '10:30' }
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientSlotSelectionPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            params: of({ doctorId: '5' }),
            queryParams: of({})
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientSlotSelectionPage);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should read doctorId from route params', () => {
    expect(component.doctorId()).toBe(5);
  });

  it('should have show times button disabled when no date selected', () => {
    const button = fixture.nativeElement.querySelector('.date-section__row .btn');
    expect(button).toBeTruthy();
    expect(button.disabled).toBe(true);
  });

  it('should load available slots for selected date', () => {
    component.selectedDate.set('2026-08-01');
    fixture.detectChanges();

    component.loadSlots();
    fixture.detectChanges();

    const req = httpMock.expectOne('/v1/patient/doctors/5/slots?date=2026-08-01');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: mockSlots });
    fixture.detectChanges();

    expect(component.slots().length).toBe(3);
  });

  it('should show no-availability message when empty slots returned', () => {
    component.selectedDate.set('2026-08-01');
    fixture.detectChanges();

    component.loadSlots();
    const req = httpMock.expectOne('/v1/patient/doctors/5/slots?date=2026-08-01');
    req.flush({ success: true, data: [] });
    fixture.detectChanges();

    const emptyMsg = fixture.nativeElement.querySelector('.empty-state');
    expect(emptyMsg).toBeTruthy();
  });

  it('should select a slot when clicked', () => {
    component.slots.set(mockSlots);
    fixture.detectChanges();

    const buttons = fixture.nativeElement.querySelectorAll('.slot-button');
    buttons[1].click();
    fixture.detectChanges();

    expect(component.selectedSlot()?.startTime).toBe('09:30');
    expect(buttons[1].classList.contains('slot-button--selected')).toBe(true);
  });

  it('should disable continue button when no slot selected', () => {
    component.slots.set(mockSlots);
    fixture.detectChanges();

    const proceedBtn = fixture.nativeElement.querySelector('.slot-page__actions .btn');
    expect(proceedBtn).toBeTruthy();
    expect(proceedBtn.disabled).toBe(true);
  });
});
