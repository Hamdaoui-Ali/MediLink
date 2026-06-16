import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { vi } from 'vitest';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTestingController: HttpTestingController;
  let router: Router;

  beforeEach(() => {
    localStorage.clear();

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    });

    service = TestBed.inject(AuthService);
    httpTestingController = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => {
    httpTestingController.verify();
    localStorage.clear();
  });

  it('should post credentials to the versioned login endpoint and store the session', () => {
    service.login({
      email: 'jane@example.com',
      password: 'Patient@123'
    }).subscribe((session) => {
      expect(session.accessToken).toBe('jwt-token');
      expect(session.user.role).toBe('PATIENT');
      expect(service.isAuthenticated()).toBeTruthy();
      expect(localStorage.getItem('medilink.auth.session')).toContain('jwt-token');
    });

    const request = httpTestingController.expectOne('/v1/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      success: true,
      data: {
        accessToken: 'jwt-token',
        tokenType: 'Bearer',
        expiresAt: '2026-06-11T22:00:00Z',
        user: {
          id: 1,
          fullName: 'Jane Patient',
          email: 'jane@example.com',
          role: 'PATIENT'
        }
      },
      error: null,
      timestamp: new Date().toISOString()
    });
  });

  it('should redirect patient users to the patient dashboard after login', () => {
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    service.login({
      email: 'jane@example.com',
      password: 'Patient@123'
    }).subscribe(() => service.redirectAfterLogin());

    const request = httpTestingController.expectOne('/v1/auth/login');
    request.flush({
      success: true,
      data: {
        accessToken: 'jwt-token',
        user: {
          email: 'jane@example.com',
          role: 'PATIENT'
        }
      },
      error: null,
      timestamp: new Date().toISOString()
    });

    expect(router.navigate).toHaveBeenCalledWith(['/patient']);
  });

  it('should clear the stored session on logout', () => {
    vi.spyOn(router, 'navigate').mockResolvedValue(true);

    service.login({
      email: 'jane@example.com',
      password: 'Patient@123'
    }).subscribe(() => service.logout());

    const request = httpTestingController.expectOne('/v1/auth/login');
    request.flush({
      success: true,
      data: {
        accessToken: 'jwt-token',
        user: {
          email: 'jane@example.com',
          role: 'PATIENT'
        }
      },
      error: null,
      timestamp: new Date().toISOString()
    });


    expect(localStorage.getItem('medilink.auth.session')).toBeNull();
    expect(service.isAuthenticated()).toBeFalsy();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });
});
