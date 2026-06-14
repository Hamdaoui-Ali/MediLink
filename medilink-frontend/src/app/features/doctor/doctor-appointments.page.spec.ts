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
  };

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
    },
    {
      id: 3,
      doctorId: 1,
      patientId: 4,
      patientName: 'Alice Brown',
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
      listDoctorAppointments: vi.fn().mockReturnValue(of(mockAppointments))
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
    expect(component.errorMessage()).toBe('');
  });

  it('should set isLoading to false after loading completes', () => {
    fixture.detectChanges();

    expect(component.isLoading()).toBeFalsy();
    expect(component.errorMessage()).toBe('');
  });

  it('should show error message when loading fails', () => {
    appointmentService.listDoctorAppointments.mockReturnValue(
      throwError(() => ({ status: 403 }))
    );

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('You do not have permission to access this page.');
    expect(component.isLoading()).toBeFalsy();
  });

  it('should show error for unauthorized access', () => {
    appointmentService.listDoctorAppointments.mockReturnValue(
      throwError(() => ({ status: 401 }))
    );

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('Your session has expired. Please log in again.');
  });

  it('should show empty state when no appointments exist', () => {
    appointmentService.listDoctorAppointments.mockReturnValue(of([]));

    fixture.detectChanges();

    expect(component.appointments().length).toBe(0);
  });

  it('should filter appointments by status via server call', () => {
    fixture.detectChanges();
    const confirmedOnly = [mockAppointments[0]];
    appointmentService.listDoctorAppointments.mockReturnValue(of(confirmedOnly));

    component.statusFilter.set('CONFIRMED');
    component.applyFilters();

    expect(appointmentService.listDoctorAppointments).toHaveBeenCalledWith('CONFIRMED', undefined, undefined);
    expect(component.appointments()).toEqual(confirmedOnly);
  });

  it('should filter appointments by date via server call', () => {
    fixture.detectChanges();
    const dateFiltered = [mockAppointments[0]];
    appointmentService.listDoctorAppointments.mockReturnValue(of(dateFiltered));

    component.dateFilter.set('2026-06-15');
    component.applyFilters();

    expect(appointmentService.listDoctorAppointments).toHaveBeenCalledWith(undefined, '2026-06-15', '2026-06-15');
    expect(component.appointments()).toEqual(dateFiltered);
  });

  it('should filter appointments by status and date via server call', () => {
    fixture.detectChanges();
    const filtered = [mockAppointments[0]];
    appointmentService.listDoctorAppointments.mockReturnValue(of(filtered));

    component.statusFilter.set('CONFIRMED');
    component.dateFilter.set('2026-06-15');
    component.applyFilters();

    expect(appointmentService.listDoctorAppointments).toHaveBeenCalledWith('CONFIRMED', '2026-06-15', '2026-06-15');
    expect(component.appointments()).toEqual(filtered);
  });

  it('should clear filters and reload all appointments', () => {
    fixture.detectChanges();
    component.statusFilter.set('CONFIRMED');
    component.dateFilter.set('2026-06-15');

    const allAppointments = [...mockAppointments];
    appointmentService.listDoctorAppointments.mockReturnValue(of(allAppointments));

    component.clearFilters();

    expect(component.statusFilter()).toBe('');
    expect(component.dateFilter()).toBe('');
    expect(appointmentService.listDoctorAppointments).toHaveBeenCalledWith(undefined, undefined, undefined);
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

  it('should return correct status label', () => {
    expect(component.statusLabel('CONFIRMED')).toBe('Confirmed');
    expect(component.statusLabel('CANCELLED')).toBe('Cancelled');
    expect(component.statusLabel('COMPLETED')).toBe('Completed');
    expect(component.statusLabel('MISSED')).toBe('Missed');
    expect(component.statusLabel('RESCHEDULED')).toBe('Rescheduled');
  });

  it('should return correct status CSS class', () => {
    expect(component.statusClass('CONFIRMED')).toBe('status-confirmed');
    expect(component.statusClass('COMPLETED')).toBe('status-completed');
    expect(component.statusClass('CANCELLED')).toBe('status-cancelled');
    expect(component.statusClass('MISSED')).toBe('status-missed');
    expect(component.statusClass('RESCHEDULED')).toBe('status-rescheduled');
  });
});
