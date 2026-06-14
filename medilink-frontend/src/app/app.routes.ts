import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './shared/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/home/home.page').then((module) => module.HomePage)
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login.page').then((module) => module.LoginPage)
  },
  {
    path: 'register',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/register.page').then((module) => module.RegisterPage)
  },
  {
    path: 'patient',
    canActivate: [authGuard],
    data: { roles: ['PATIENT'] },
    loadComponent: () =>
      import('./features/patient/patient-dashboard.page').then((module) => module.PatientDashboardPage)
  },
  {
    path: 'doctor',
    canActivate: [authGuard],
    data: { roles: ['DOCTOR'] },
    loadComponent: () =>
      import('./features/doctor/doctor-dashboard.page').then((module) => module.DoctorDashboardPage)
  },
  {
    path: 'doctor/appointments',
    canActivate: [authGuard],
    data: { roles: ['DOCTOR'] },
    loadComponent: () =>
      import('./features/doctor/doctor-appointments.page').then((module) => module.DoctorAppointmentsPage)
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/admin/admin-dashboard.page').then((module) => module.AdminDashboardPage)
  },
  {
    path: 'admin/specialties',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/admin/admin-specialty-management.page').then((module) => module.AdminSpecialtyManagementPage)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
