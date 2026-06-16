import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { ApiService } from './api.service';
import { AuthRole, AuthSession, AuthUser, LoginRequest, LoginResponse } from '../models/auth.model';

const AUTH_STORAGE_KEY = 'medilink.auth.session';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private readonly api = inject(ApiService);
  private readonly router = inject(Router);
  private readonly sessionState = signal<AuthSession | null>(this.readStoredSession());

  readonly session = this.sessionState.asReadonly();
  readonly isAuthenticated = computed(() => !!this.sessionState()?.accessToken);
  readonly currentUser = computed(() => this.sessionState()?.user ?? null);
  readonly currentRole = computed(() => this.sessionState()?.user.role ?? null);

  login(credentials: LoginRequest): Observable<AuthSession> {
    return this.api.post<LoginResponse>('/v1/auth/login', credentials).pipe(
      map((response) => this.toSession((response.data ?? response) as LoginResponse)),
      tap((session) => this.setSession(session))
    );
  }

  logout(redirectToLogin = true): void {
    this.sessionState.set(null);
    localStorage.removeItem(AUTH_STORAGE_KEY);

    if (redirectToLogin) {
      this.router.navigate(['/login']);
    }
  }

  getAccessToken(): string | null {
    return this.sessionState()?.accessToken ?? null;
  }

  getDefaultRouteForRole(role: AuthRole): string {
    const routes: Record<AuthRole, string> = {
      ADMIN: '/admin',
      DOCTOR: '/doctor',
      PATIENT: '/patient'
    };

    return routes[role];
  }

  hasAnyRole(allowedRoles: AuthRole[]): boolean {
    const role = this.currentRole();
    return !!role && allowedRoles.includes(role);
  }

  redirectAfterLogin(): void {
    const role = this.currentRole();
    this.router.navigate([role ? this.getDefaultRouteForRole(role) : '/']);
  }

  private setSession(session: AuthSession): void {
    this.sessionState.set(session);
    localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
  }

  private readStoredSession(): AuthSession | null {
    const storedValue = localStorage.getItem(AUTH_STORAGE_KEY);

    if (!storedValue) {
      return null;
    }

    try {
      const session = JSON.parse(storedValue) as AuthSession;
      return session.accessToken && session.user?.role ? session : null;
    } catch {
      localStorage.removeItem(AUTH_STORAGE_KEY);
      return null;
    }
  }

  private toSession(response: LoginResponse): AuthSession {
    const accessToken = response.accessToken ?? response.token ?? response.jwt;
    const role = response.user?.role ?? response.role ?? response.roles?.[0];
    const email = response.user?.email ?? response.email;

    if (!accessToken || !role || !email) {
      throw new Error('Login response is missing token, role, or email.');
    }

    const user: AuthUser = {
      id: response.user?.id,
      fullName: response.user?.fullName ?? response.fullName,
      email,
      role
    };

    return {
      accessToken,
      tokenType: response.tokenType ?? 'Bearer',
      expiresAt: response.expiresAt,
      user
    };
  }
}
