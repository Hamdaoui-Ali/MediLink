import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { AppointmentService } from './appointment.service';
import { Appointment } from '../models/appointment.model';

describe('AppointmentService', () => {
  let service: AppointmentService;
  let httpTesting: HttpTestingController;

  const mockAppointments: Appointment[] = [
    {
      id: 1,
      doctorId: 1,
      patientId: 2,
      patientName: 'Jane Patient',
      appointmentDate: '2026-06-15',
      startTime: '10:00:00',
      endTime: '10:30:00',
      status: 'CONFIRMED',
      reason: 'Routine checkup',
      doctorNotes: null
    },
    {
      id: 2,
      doctorId: 1,
      patientId: 3,
      patientName: 'John Smith',
      appointmentDate: '2026-06-16',
      startTime: '14:00:00',
      endTime: '14:30:00',
      status: 'COMPLETED',
      reason: 'Follow-up visit',
      doctorNotes: 'All clear'
    }
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AppointmentService,
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AppointmentService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTesting.verify();
  });

  it('should list all doctor appointments when no status filter is provided', () => {
    service.listDoctorAppointments().subscribe((appointments) => {
      expect(appointments.length).toBe(2);
      expect(appointments[0].patientName).toBe('Jane Patient');
    });

    const req = httpTesting.expectOne('/v1/doctor/appointments');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: mockAppointments });
  });

  it('should list doctor appointments filtered by status', () => {
    service.listDoctorAppointments('CONFIRMED').subscribe((appointments) => {
      expect(appointments.length).toBe(1);
      expect(appointments[0].status).toBe('CONFIRMED');
    });

    const req = httpTesting.expectOne('/v1/doctor/appointments?status=CONFIRMED');
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: [mockAppointments[0]] });
  });

  it('should list doctor appointments filtered by date range', () => {
    service.listDoctorAppointments(undefined, '2026-06-01', '2026-06-30').subscribe((appointments) => {
      expect(appointments.length).toBe(1);
    });

    const req = httpTesting.expectOne(
      '/v1/doctor/appointments?from=2026-06-01&to=2026-06-30'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: [mockAppointments[0]] });
  });

  it('should list doctor appointments filtered by status and date range', () => {
    service.listDoctorAppointments('COMPLETED', '2026-06-01', '2026-06-30').subscribe((appointments) => {
      expect(appointments.length).toBe(1);
      expect(appointments[0].status).toBe('COMPLETED');
    });

    const req = httpTesting.expectOne(
      '/v1/doctor/appointments?status=COMPLETED&from=2026-06-01&to=2026-06-30'
    );
    expect(req.request.method).toBe('GET');
    req.flush({ success: true, data: [mockAppointments[1]] });
  });

  it('should handle API response without data envelope', () => {
    service.listDoctorAppointments().subscribe((appointments) => {
      expect(appointments.length).toBe(1);
    });

    const req = httpTesting.expectOne('/v1/doctor/appointments');
    req.flush([mockAppointments[0]]);
  });
});
