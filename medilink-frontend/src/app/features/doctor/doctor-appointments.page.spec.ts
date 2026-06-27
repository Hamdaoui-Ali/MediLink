import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { Appointment } from '../../shared/models/appointment.model';
import { AppointmentService } from '../../shared/services/appointment.service';
import { DoctorAppointmentsPage } from './doctor-appointments.page';

describe('DoctorAppointmentsPage', () => {
  let fixture: ComponentFixture<DoctorAppointmentsPage>;
  let component: DoctorAppointmentsPage;
  let appointmentService: {
    listDoctorAppointments: ReturnType<typeof vi.fn>;
    listDoctorPatientHistory: ReturnType<typeof vi.fn>;
    updateNotes: ReturnType<typeof vi.fn>;
    updateStatus: ReturnType<typeof vi.fn>;
  };

  const mockAppointments: Appointment[] = [
    {
      id: 1,
      doctorId: 1,
      patientId: 2,
      patientName: 'Jane Patient',
      patientEmail: 'jane.patient@medilink.local',
      patientPhoneNumber: '+15551234567',
      patientDateOfBirth: '1990-01-15',
      patientGender: 'FEMALE',
      patientAddress: '123 Patient St',
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
      patientEmail: 'john.smith@medilink.local',
      patientPhoneNumber: null,
      patientDateOfBirth: null,
      patientGender: null,
      patientAddress: null,
      appointmentDate: '2026-06-16',
      startTime: '14:00:00',
      endTime: '14:30:00',
      status: 'COMPLETED',
      reason: 'Follow-up visit',
      doctorNotes: 'All clear'
    },
    {
      id: 3,
      doctorId: 1,
      patientId: 4,
      patientName: 'Alice Brown',
      patientEmail: null,
      patientPhoneNumber: null,
      patientDateOfBirth: null,
      patientGender: null,
      patientAddress: null,
      appointmentDate: '2026-06-14',
      startTime: '09:00:00',
      endTime: '09:30:00',
      status: 'CANCELLED',
      reason: 'Headache',
      doctorNotes: null
    }
  ];

  beforeEach(async () => {
    appointmentService = {
      listDoctorAppointments: vi.fn().mockReturnValue(of(mockAppointments)),
      listDoctorPatientHistory: vi.fn().mockReturnValue(of([mockAppointments[1], mockAppointments[0]])),
      updateNotes: vi.fn(),
      updateStatus: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [DoctorAppointmentsPage],
      providers: [
        provideRouter([]),
        {
          provide: AppointmentService,
          useValue: appointmentService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorAppointmentsPage);
    component = fixture.componentInstance;
  });

  it('should load appointments on init', () => {
    fixture.detectChanges();

    expect(appointmentService.listDoctorAppointments).toHaveBeenCalledWith(undefined, undefined, undefined);
    expect(component.appointments().length).toBe(3);
    expect(component.isLoading()).toBeFalsy();
  });

  it('should show error message when loading fails', () => {
    appointmentService.listDoctorAppointments.mockReturnValue(
      throwError(() => ({ status: 403 }))
    );

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('You do not have permission to access this page.');
  });

  it('should select an appointment and populate notes draft', () => {
    fixture.detectChanges();

    component.selectAppointment(mockAppointments[1]);

    expect(component.selectedAppointment()?.id).toBe(2);
    expect(component.notesDraft()).toBe('All clear');
    expect(appointmentService.listDoctorPatientHistory).toHaveBeenCalledWith(3);
    expect(component.patientHistory().length).toBe(2);
    expect(component.detailMessage()).toBe('');
    expect(component.detailError()).toBe('');
  });

  it('should clear selection', () => {
    component.selectAppointment(mockAppointments[0]);
    component.clearSelection();

    expect(component.selectedAppointment()).toBeNull();
    expect(component.patientHistory().length).toBe(0);
  });

  it('should save notes and update the appointment in the list', () => {
    fixture.detectChanges();
    component.selectAppointment(mockAppointments[0]);
    component.notesDraft.set('New private notes');

    const updated: Appointment = { ...mockAppointments[0], doctorNotes: 'New private notes' };
    appointmentService.updateNotes.mockReturnValue(of(updated));

    component.saveNotes();

    expect(appointmentService.updateNotes).toHaveBeenCalledWith(1, 'New private notes');
    expect(component.selectedAppointment()?.doctorNotes).toBe('New private notes');
    expect(component.detailMessage()).toBe('Notes saved.');
  });

  it('should show error when saving notes fails', () => {
    fixture.detectChanges();
    component.selectAppointment(mockAppointments[0]);

    appointmentService.updateNotes.mockReturnValue(throwError(() => ({ status: 400 })));

    component.saveNotes();

    expect(component.detailError()).toBe('Invalid operation. The status transition may not be allowed.');
    expect(component.isSavingNotes()).toBeFalsy();
  });

  it('should update status and update the appointment in the list', () => {
    fixture.detectChanges();
    component.selectAppointment(mockAppointments[0]);

    const updated: Appointment = { ...mockAppointments[0], status: 'COMPLETED' as const };
    appointmentService.updateStatus.mockReturnValue(of(updated));

    component.updateStatus('COMPLETED');

    expect(appointmentService.updateStatus).toHaveBeenCalledWith(1, 'COMPLETED');
    expect(component.selectedAppointment()?.status).toBe('COMPLETED');
    expect(component.detailMessage()).toBe('Appointment marked as Completed.');
  });

  it('should return valid transitions for non-terminal status', () => {
    fixture.detectChanges();
    component.selectAppointment(mockAppointments[0]);

    const transitions = component.validTransitions();
    expect(transitions).toContain('COMPLETED');
    expect(transitions).toContain('CANCELLED');
    expect(transitions).toContain('MISSED');
    expect(transitions).toContain('RESCHEDULED');
    expect(transitions.length).toBe(4);
  });

  it('should return empty transitions for terminal status', () => {
    fixture.detectChanges();
    component.selectAppointment(mockAppointments[1]);

    expect(component.validTransitions().length).toBe(0);
    expect(component.isTerminal()).toBe(true);
  });

  it('should return false for isTerminal when no appointment selected', () => {
    expect(component.isTerminal()).toBe(false);
  });

  it('should format time correctly', () => {
    expect(component.formatTime('10:00:00')).toBe('10:00 AM');
    expect(component.formatTime('14:30:00')).toBe('2:30 PM');
    expect(component.formatTime('')).toBe('');
  });

  it('should format date correctly', () => {
    const formatted = component.formatDate('2026-06-15');
    expect(formatted).toContain('2026');
    expect(formatted).toContain('Jun');
    expect(formatted).toContain('15');
  });

  it('should format patient demographics', () => {
    expect(component.formatGender('FEMALE')).toBe('Female');
    expect(component.formatGender(null)).toBe('Not recorded');
    expect(component.contactValue('')).toBe('Not recorded');
    expect(component.formatAge(null)).toBe('Not recorded');
  });

  it('should return correct status label', () => {
    expect(component.statusLabel('CONFIRMED')).toBe('Confirmed');
    expect(component.statusLabel('COMPLETED')).toBe('Completed');
    expect(component.statusLabel('CANCELLED')).toBe('Cancelled');
    expect(component.statusLabel('MISSED')).toBe('Missed');
    expect(component.statusLabel('RESCHEDULED')).toBe('Rescheduled');
  });
});
