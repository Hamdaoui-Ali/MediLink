import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './shared/services/auth.service';
import { NavigationItem } from './shared/models/navigation-item.model';

const ALL_NAV_ITEMS: NavigationItem[] = [
  { label: 'Home', route: '/' },
  { label: 'Find Doctor', route: '/patient/doctors', roles: ['PATIENT'] },
  { label: 'Sign In', route: '/login', guestOnly: true },
  { label: 'Create Account', route: '/register', guestOnly: true },
  { label: 'Dashboard', route: '/patient', roles: ['PATIENT'] },
  { label: 'Book Appointment', route: '/patient/book', roles: ['PATIENT'] },
  { label: 'My Appointments', route: '/patient/appointments', roles: ['PATIENT'] },
  { label: 'Dashboard', route: '/doctor', roles: ['DOCTOR'] },
  { label: 'Appointments', route: '/doctor/appointments', roles: ['DOCTOR'] },
  { label: 'Availability', route: '/doctor/availability', roles: ['DOCTOR'] },
  { label: 'Blocked Slots', route: '/doctor/blocked-slots', roles: ['DOCTOR'] },
  { label: 'Profile', route: '/doctor/profile', roles: ['DOCTOR'] },
  { label: 'Dashboard', route: '/admin', roles: ['ADMIN'] },
  { label: 'Specialties', route: '/admin/specialties', roles: ['ADMIN'] },
  { label: 'Doctors', route: '/admin/doctors', roles: ['ADMIN'] },
  { label: 'Patients', route: '/admin/patients', roles: ['ADMIN'] },
  { label: 'Appointments', roles: ['ADMIN'], comingSoon: true },
  { label: 'Activity Log', roles: ['ADMIN'], comingSoon: true },
  { label: 'Logout', action: 'logout' },
];

@Component({
  selector: 'app-root',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  protected readonly authService = inject(AuthService);
  protected mobileMenuOpen = signal(false);

  protected readonly visibleNavItems = computed(() =>
    ALL_NAV_ITEMS.filter((item) => {
      if (item.action === 'logout') {
        return this.authService.isAuthenticated();
      }
      if (item.guestOnly) {
        return !this.authService.isAuthenticated();
      }
      if (item.roles) {
        const role = this.authService.currentRole();
        return role !== null && item.roles.includes(role);
      }
      return true;
    })
  );

  protected readonly sidebarContext = computed(() => {
    const role = this.authService.currentRole();
    if (!role) return { title: 'Welcome to MediLink', subtitle: 'Your trusted healthcare scheduling platform. Sign in or create an account to access your workspace.' };
    if (role === 'PATIENT') return { title: 'Patient Workspace', subtitle: 'Find doctors, book appointments, and manage your healthcare schedule.' };
    if (role === 'DOCTOR') return { title: 'Doctor Workspace', subtitle: 'Manage your appointments, blocked slots, and professional profile.' };
    return { title: 'Admin Workspace', subtitle: 'Oversee specialties, doctors, patients, and platform activity.' };
  });

  protected readonly roleBadge = computed(() => {
    const role = this.authService.currentRole();
    if (!role) return null;
    return { PATIENT: 'Patient', DOCTOR: 'Doctor', ADMIN: 'Admin' }[role];
  });

  protected toggleMobileMenu(): void {
    this.mobileMenuOpen.update((v) => !v);
  }

  protected closeMobileMenu(): void {
    this.mobileMenuOpen.set(false);
  }

  protected handleNavAction(item: NavigationItem): void {
    if (item.action === 'logout') {
      this.authService.logout();
    }
  }
}
