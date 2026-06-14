import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { DoctorProfile } from '../../shared/models/doctor-profile.model';
import { DoctorProfileService } from '../../shared/services/doctor-profile.service';
import { DoctorProfilePage } from './doctor-profile.page';

describe('DoctorProfilePage', () => {
  let fixture: ComponentFixture<DoctorProfilePage>;
  let component: DoctorProfilePage;
  let profileService: {
    getProfile: ReturnType<typeof vi.fn>;
    updateProfile: ReturnType<typeof vi.fn>;
  };

  const mockProfile: DoctorProfile = {
    id: 1,
    userId: 5,
    fullName: 'Dr. Test',
    email: 'dr.test@medilink.local',
    phoneNumber: '+15551234567',
    accountStatus: 'ACTIVE',
    specialtyName: 'Cardiology',
    biography: 'Experienced cardiologist',
    consultationDurationMinutes: 30,
    clinicAddress: '123 Heart Lane',
    status: 'ACTIVE'
  };

  beforeEach(async () => {
    profileService = {
      getProfile: vi.fn().mockReturnValue(of(mockProfile)),
      updateProfile: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [DoctorProfilePage],
      providers: [
        provideRouter([]),
        { provide: DoctorProfileService, useValue: profileService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DoctorProfilePage);
    component = fixture.componentInstance;
  });

  it('should load profile on init', () => {
    fixture.detectChanges();

    expect(profileService.getProfile).toHaveBeenCalled();
    expect(component.profile()?.fullName).toBe('Dr. Test');
    expect(component.isLoading()).toBeFalsy();
  });

  it('should populate edit form with profile data', () => {
    fixture.detectChanges();

    expect(component.editForm.getRawValue().biography).toBe('Experienced cardiologist');
    expect(component.editForm.getRawValue().clinicAddress).toBe('123 Heart Lane');
    expect(component.editForm.getRawValue().consultationDurationMinutes).toBe(30);
  });

  it('should show error when loading fails', () => {
    profileService.getProfile.mockReturnValue(
      throwError(() => ({ status: 403 }))
    );

    fixture.detectChanges();

    expect(component.errorMessage()).toBe('You do not have permission to access this page.');
  });

  it('should save profile and update data', () => {
    fixture.detectChanges();
    const updated = { ...mockProfile, biography: 'Updated bio' };
    profileService.updateProfile.mockReturnValue(of(updated));

    component.editForm.patchValue({ biography: 'Updated bio' });
    component.saveProfile();

    expect(profileService.updateProfile).toHaveBeenCalledWith({
      biography: 'Updated bio',
      clinicAddress: '123 Heart Lane',
      phoneNumber: '+15551234567',
      consultationDurationMinutes: 30
    });
    expect(component.profile()?.biography).toBe('Updated bio');
    expect(component.successMessage()).toBe('Profile updated.');
  });

  it('should not submit invalid form', () => {
    fixture.detectChanges();
    component.editForm.patchValue({ consultationDurationMinutes: 0 });
    component.saveProfile();

    expect(profileService.updateProfile).not.toHaveBeenCalled();
  });

  it('should show error when saving fails', () => {
    fixture.detectChanges();
    profileService.updateProfile.mockReturnValue(
      throwError(() => ({ status: 401 }))
    );

    component.saveProfile();

    expect(component.errorMessage()).toBe('Your session has expired. Please log in again.');
    expect(component.isSaving()).toBeFalsy();
  });

  it('should trim whitespace from text fields on save', () => {
    fixture.detectChanges();
    profileService.updateProfile.mockReturnValue(of(mockProfile));

    component.editForm.setValue({
      biography: '  Bio with spaces  ',
      clinicAddress: '  Address  ',
      phoneNumber: '  +15559999999  ',
      consultationDurationMinutes: 30
    });
    component.saveProfile();

    expect(profileService.updateProfile).toHaveBeenCalledWith({
      biography: 'Bio with spaces',
      clinicAddress: 'Address',
      phoneNumber: '+15559999999',
      consultationDurationMinutes: 30
    });
  });
});
