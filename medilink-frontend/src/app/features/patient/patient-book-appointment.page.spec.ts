import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
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
      providers: [provideHttpClient(), provideHttpClientTesting()]
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

  it('should show the booking form initially', () => {
    const form = fixture.nativeElement.querySelector('.booking-form');
    expect(form).toBeTruthy();

    const confirmation = fixture.nativeElement.querySelector('.confirmation');
    expect(confirmation).toBeFalsy();
  });

  it('should disable submit button when fields are empty', () => {
    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeTrue();
  });

  it('should show error when submitting with empty fields', () => {
    component.book();
    fixture.detectChanges();

    expect(component.errorMessage()).toBe('Please fill in all fields.');
  });

  it('should book appointment successfully', () => {
    component.doctorId.set(5);
    component.appointmentDate.set('2026-07-20');
    component.startTime.set('10:00');
    component.reason.set('Annual checkup');
    fixture.detectChanges();

    const button = fixture.nativeElement.querySelector('button[type="submit"]');
    expect(button.disabled).toBeFalse();

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

    expect(component.successMessage()).toBe('Your appointment has been booked successfully!');
    expect(component.bookedAppointment()).toEqual(mockAppointment);

    const confirmation = fixture.nativeElement.querySelector('.confirmation');
    expect(confirmation).toBeTruthy();
  });

  it('should handle booking conflict error', () => {
    component.doctorId.set(5);
    component.appointmentDate.set('2026-07-20');
    component.startTime.set('10:00');
    component.reason.set('Annual checkup');
    fixture.detectChanges();

    component.book();
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
    component.successMessage.set('Booked!');
    fixture.detectChanges();

    component.reset();
    fixture.detectChanges();

    expect(component.bookedAppointment()).toBeNull();
    expect(component.successMessage()).toBe('');
    expect(component.doctorId()).toBeNull();
    expect(component.appointmentDate()).toBe('');
    expect(component.reason()).toBe('');

    const form = fixture.nativeElement.querySelector('.booking-form');
    expect(form).toBeTruthy();
  });

  it('should show confirmation details after successful booking', () => {
    component.bookedAppointment.set(mockAppointment);
    component.successMessage.set('Your appointment has been booked successfully!');
    fixture.detectChanges();

    const details = fixture.nativeElement.querySelector('.confirmation-card');
    expect(details.textContent).toContain('Annual checkup');
    expect(details.textContent).toContain('CONFIRMED');
  });
});
