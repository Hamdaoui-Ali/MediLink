import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { PatientBookAppointmentPage } from './patient-book-appointment.page';
import { Appointment } from '../../shared/models/appointment.model';

describe('PatientBookAppointmentPage', () => {
  let component: PatientBookAppointmentPage;
  let fixture: ComponentFixture<PatientBookAppointmentPage>;
  let httpMock: HttpTestingController;

  const mockAppointment: Appointment = {
    id: 1,
    doctorId: 5,
    patientId: 3,
    patientName: 'John Doe',
    patientEmail: 'john.doe@medilink.local',
    patientPhoneNumber: '+15551234567',
    patientDateOfBirth: '1991-02-03',
    patientGender: 'MALE',
    patientAddress: '123 Main St',
    appointmentDate: '2026-07-20',
    startTime: '10:00:00',
    endTime: '10:30:00',
    status: 'CONFIRMED',
    reason: 'Annual checkup',
    doctorNotes: null
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PatientBookAppointmentPage],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            queryParams: of({
              doctorId: '5',
              date: '2026-07-20',
              startTime: '10:00',
              endTime: '10:30'
            })
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PatientBookAppointmentPage);
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

  it('should pre-fill doctor context from query params', () => {
    expect(component.doctorId()).toBe(5);
    expect(component.appointmentDate()).toBe('2026-07-20');
    expect(component.startTime()).toBe('10:00');
    expect(component.preFilled()).toBe(true);
  });

  it('should disable confirm button when reason is empty', () => {
    component.reason.set('');
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.confirmation-summary .btn-primary');
    expect(button).toBeTruthy();
    expect(button.disabled).toBe(true);
  });

  it('should enable confirm button when reason is filled', () => {
    component.reason.set('Annual checkup');
    fixture.detectChanges();
    const button = fixture.nativeElement.querySelector('.confirmation-summary .btn-primary');
    expect(button.disabled).toBe(false);
  });

  it('should book appointment successfully', () => {
    component.reason.set('Annual checkup');
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.confirmation-summary .btn-primary');
    button.click();
    fixture.detectChanges();

    const req = httpMock.expectOne('/v1/patient/appointments');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      doctorId: 5,
      appointmentDate: '2026-07-20',
      startTime: '10:00:00',
      reason: 'Annual checkup'
    });

    req.flush({ success: true, data: mockAppointment });
    fixture.detectChanges();

    expect(component.bookedAppointment()).toEqual(mockAppointment);
    expect(component.isSubmitting()).toBe(false);

    const successCard = fixture.nativeElement.querySelector('.success-card');
    expect(successCard).toBeTruthy();
  });

  it('should handle booking conflict error', () => {
    component.reason.set('Annual checkup');
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('.confirmation-summary .btn-primary');
    button.click();
    fixture.detectChanges();

    const req = httpMock.expectOne('/v1/patient/appointments');
    req.flush(
      { success: false, error: { code: 'SLOT_UNAVAILABLE', message: 'Slot taken' } },
      { status: 409, statusText: 'Conflict' }
    );
    fixture.detectChanges();

    expect(component.errorMessage()).toContain('no longer available');
    expect(component.bookedAppointment()).toBeNull();
  });

  it('should reset form after booking another', () => {
    component.bookedAppointment.set(mockAppointment);
    component.doctorName.set('Dr. Smith');
    fixture.detectChanges();

    component.reset();
    fixture.detectChanges();

    expect(component.bookedAppointment()).toBeNull();
    expect(component.doctorId()).toBeNull();
    expect(component.doctorName()).toBe('');
    expect(component.reason()).toBe('');
    expect(component.preFilled()).toBe(false);
  });

  it('should show success details after successful booking', () => {
    component.bookedAppointment.set(mockAppointment);
    fixture.detectChanges();

    const successCard = fixture.nativeElement.querySelector('.success-card');
    expect(successCard).toBeTruthy();
    expect(successCard.textContent).toContain('Annual checkup');
    expect(successCard.textContent).toContain('CONFIRMED');
  });
});
