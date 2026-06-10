import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { RegisterPage } from './register.page';
import { PatientRegistrationService } from '../../shared/services/patient-registration.service';

describe('RegisterPage', () => {
  let fixture: ComponentFixture<RegisterPage>;
  let component: RegisterPage;
  let registrationService: { register: ReturnType<typeof vi.fn> };
  let router: Router;

  beforeEach(async () => {
    registrationService = {
      register: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [RegisterPage],
      providers: [
        provideRouter([]),
        {
          provide: PatientRegistrationService,
          useValue: registrationService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterPage);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigate').mockResolvedValue(true);
  });

  it('should not submit when the form is invalid', () => {
    component.submit();

    expect(registrationService.register).not.toHaveBeenCalled();
  });

  it('should normalize registration form values into an API request', () => {
    component.registrationForm.setValue({
      fullName: '  Jane Patient  ',
      email: '  JANE@Example.COM ',
      password: 'Patient@123',
      phoneNumber: '  +15551234567 ',
      dateOfBirth: '',
      gender: '',
      address: '   '
    });

    expect(component.toRequest()).toEqual({
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      password: 'Patient@123',
      phoneNumber: '+15551234567',
      dateOfBirth: null,
      gender: null,
      address: null
    });
  });

  it('should register and redirect to login with the patient email', () => {
    component.registrationForm.setValue({
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      password: 'Patient@123',
      phoneNumber: '+15551234567',
      dateOfBirth: '1990-03-05',
      gender: 'FEMALE',
      address: '100 Care Street'
    });
    registrationService.register.mockReturnValue(of({
      userId: 1,
      patientId: 2,
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      phoneNumber: '+15551234567',
      role: 'PATIENT',
      accountStatus: 'ACTIVE'
    }));

    component.submit();

    expect(registrationService.register).toHaveBeenCalledWith({
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      password: 'Patient@123',
      phoneNumber: '+15551234567',
      dateOfBirth: '1990-03-05',
      gender: 'FEMALE',
      address: '100 Care Street'
    });
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {
      queryParams: {
        registered: 'true',
        email: 'jane@example.com'
      }
    });
  });

  it('should show duplicate email errors clearly', () => {
    component.registrationForm.setValue({
      fullName: 'Jane Patient',
      email: 'jane@example.com',
      password: 'Patient@123',
      phoneNumber: '+15551234567',
      dateOfBirth: '',
      gender: '',
      address: ''
    });
    registrationService.register.mockReturnValue(throwError(() => ({ status: 409 })));

    component.submit();

    expect(component.errorMessage()).toBe('An account already exists for this email.');
    expect(component.isSubmitting()).toBeFalsy();
  });
});
