import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { LoginPage } from './login.page';
import { AuthService } from '../../shared/services/auth.service';

describe('LoginPage', () => {
  let fixture: ComponentFixture<LoginPage>;
  let component: LoginPage;
  let authService: {
    login: ReturnType<typeof vi.fn>;
    redirectAfterLogin: ReturnType<typeof vi.fn>;
  };
  let router: Router;

  beforeEach(async () => {
    authService = {
      login: vi.fn(),
      redirectAfterLogin: vi.fn()
    };

    await TestBed.configureTestingModule({
      imports: [LoginPage],
      providers: [
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              queryParamMap: {
                get: vi.fn().mockReturnValue(null)
              }
            }
          }
        },
        {
          provide: AuthService,
          useValue: authService
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginPage);
    component = fixture.componentInstance;
    router = TestBed.inject(Router);
    vi.spyOn(router, 'navigateByUrl').mockResolvedValue(true);
  });

  it('should login and redirect through the auth service', () => {
    component.loginForm.setValue({
      email: 'jane@example.com',
      password: 'Patient@123'
    });
    authService.login.mockReturnValue(of({
      accessToken: 'token',
      tokenType: 'Bearer',
      user: {
        email: 'jane@example.com',
        role: 'PATIENT'
      }
    }));

    component.submit();

    expect(authService.login).toHaveBeenCalledWith({
      email: 'jane@example.com',
      password: 'Patient@123'
    });
    expect(authService.redirectAfterLogin).toHaveBeenCalled();
  });

  it('should show server failures separately from invalid credentials', () => {
    component.loginForm.setValue({
      email: 'jane@example.com',
      password: 'Patient@123'
    });
    authService.login.mockReturnValue(throwError(() => new HttpErrorResponse({ status: 500 })));

    component.submit();

    expect(component.errorMessage()).toBe('The service is temporarily unavailable. Please try again in a moment.');
    expect(component.isSubmitting()).toBeFalsy();
  });
});
