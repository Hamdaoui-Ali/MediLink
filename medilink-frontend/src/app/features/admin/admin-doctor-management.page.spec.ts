import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { AdminDoctorManagementPage } from './admin-doctor-management.page';
import { AdminDoctor } from '../../shared/models/admin-doctor.model';
import { AdminDoctorManagementService } from '../../shared/services/admin-doctor-management.service';
import { SpecialtyLookupService } from '../../shared/services/specialty-lookup.service';

describe('AdminDoctorManagementPage', () => {
  let fixture: ComponentFixture<AdminDoctorManagementPage>;
  let component: AdminDoctorManagementPage;
  let doctorService: {
    list: ReturnType<typeof vi.fn>;
    create: ReturnType<typeof vi.fn>;
    update: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
    deactivate: ReturnType<typeof vi.fn>;
  };
  let specialtyService: {
    listActive: ReturnType<typeof vi.fn>;
  };

  const doctor: AdminDoctor = {
    id: 1,
    fullName: 'Dr Care',
    email: 'doctor@example.com',
    phoneNumber: '+15551234567',
    specialtyId: 2,
    specialtyName: 'Cardiology',
    biography: 'Heart specialist',
    consultationDurationMinutes: 30,
    clinicAddress: '100 Clinic Street',
    status: 'ACTIVE'
  };

  beforeEach(async () => {
    doctorService = {
      list: vi.fn().mockReturnValue(of([doctor])),
      create: vi.fn(),
      update: vi.fn(),
      activate: vi.fn(),
      deactivate: vi.fn()
    };
    specialtyService = {
      listActive: vi.fn().mockReturnValue(of([
        {
          id: 2,
          name: 'Cardiology',
          status: 'ACTIVE'
        }
      ]))
    };

    await TestBed.configureTestingModule({
      imports: [AdminDoctorManagementPage],
      providers: [
        provideRouter([]),
        {
          provide: AdminDoctorManagementService,
          useValue: doctorService
        },
        {
          provide: SpecialtyLookupService,
          useValue: specialtyService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AdminDoctorManagementPage);
    component = fixture.componentInstance;
  });

  it('should load doctors and specialties on init', () => {
    fixture.detectChanges();

    expect(component.doctors()).toEqual([doctor]);
    expect(component.specialties()).toEqual([{ id: 2, name: 'Cardiology' }]);
    expect(doctorService.list).toHaveBeenCalled();
    expect(specialtyService.listActive).toHaveBeenCalled();
  });

  it('should create a doctor with normalized form values', () => {
    const createdDoctor: AdminDoctor = {
      ...doctor,
      id: 3,
      fullName: 'Dr New',
      email: 'new@example.com'
    };
    doctorService.create.mockReturnValue(of(createdDoctor));
    fixture.detectChanges();

    component.doctorForm.setValue({
      fullName: '  Dr New  ',
      email: 'NEW@Example.COM',
      password: 'Doctor@123',
      phoneNumber: '  +15557654321 ',
      specialtyId: '2',
      biography: '  General care ',
      consultationDurationMinutes: 45,
      clinicAddress: '  200 Clinic Street '
    });

    component.submit();

    expect(doctorService.create).toHaveBeenCalledWith({
      fullName: 'Dr New',
      email: 'new@example.com',
      password: 'Doctor@123',
      phoneNumber: '+15557654321',
      specialtyId: 2,
      biography: 'General care',
      consultationDurationMinutes: 45,
      clinicAddress: '200 Clinic Street'
    });
    expect(component.doctors()[0]).toEqual(createdDoctor);
  });

  it('should edit an existing doctor without requiring a password', () => {
    const updatedDoctor: AdminDoctor = {
      ...doctor,
      fullName: 'Dr Care Updated'
    };
    doctorService.update.mockReturnValue(of(updatedDoctor));
    fixture.detectChanges();

    component.edit(doctor);
    component.doctorForm.patchValue({
      fullName: 'Dr Care Updated'
    });
    component.submit();

    expect(doctorService.update).toHaveBeenCalledWith(1, expect.objectContaining({
      fullName: 'Dr Care Updated',
      password: undefined
    }));
    expect(component.doctors()[0].fullName).toBe('Dr Care Updated');
  });

  it('should activate and deactivate doctors', () => {
    fixture.detectChanges();
    doctorService.deactivate.mockReturnValue(of({ ...doctor, status: 'INACTIVE' }));
    doctorService.activate.mockReturnValue(of(doctor));

    component.deactivate(doctor);

    expect(doctorService.deactivate).toHaveBeenCalledWith(1);
    expect(component.doctors()[0].status).toBe('INACTIVE');

    component.activate({ ...doctor, status: 'INACTIVE' });

    expect(doctorService.activate).toHaveBeenCalledWith(1);
    expect(component.doctors()[0].status).toBe('ACTIVE');
  });

  it('should show permission errors clearly', () => {
    doctorService.list.mockReturnValue(throwError(() => ({ status: 403 })));

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('Only admins can manage doctors.');
  });
});
