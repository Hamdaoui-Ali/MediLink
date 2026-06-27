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
    path: 'patient/book',
    canActivate: [authGuard],
    data: { roles: ['PATIENT'] },
    loadComponent: () =>
      import('./features/patient/patient-book-appointment.page').then((module) => module.PatientBookAppointmentPage)
  },
  {
    path: 'patient/slots/:doctorId',
    canActivate: [authGuard],
    data: { roles: ['PATIENT'] },
    loadComponent: () =>
      import('./features/patient/patient-slot-selection.page').then((module) => module.PatientSlotSelectionPage)
  },
  {
    path: 'patient/doctors',
    canActivate: [authGuard],
    data: { roles: ['PATIENT'] },
    loadComponent: () =>
      import('./features/patient/patient-doctor-search.page').then((module) => module.PatientDoctorSearchPage)
  },
  {
    path: 'patient/appointments',
    canActivate: [authGuard],
    data: { roles: ['PATIENT'] },
    loadComponent: () =>
      import('./features/patient/patient-appointments.page').then((module) => module.PatientAppointmentsPage)
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
    path: 'doctor/blocked-slots',
    canActivate: [authGuard],
    data: { roles: ['DOCTOR'] },
    loadComponent: () =>
      import('./features/doctor/doctor-blocked-slots.page').then((module) => module.DoctorBlockedSlotsPage)
  },
  {
    path: 'doctor/availability',
    canActivate: [authGuard],
    data: { roles: ['DOCTOR'] },
    loadComponent: () =>
      import('./features/doctor/doctor-availability.page').then((module) => module.DoctorAvailabilityPage)
  },
  {
    path: 'doctor/profile',
    canActivate: [authGuard],
    data: { roles: ['DOCTOR'] },
    loadComponent: () =>
      import('./features/doctor/doctor-profile.page').then((module) => module.DoctorProfilePage)
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
    path: 'admin/doctors',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/admin/admin-doctor-management.page').then((module) => module.AdminDoctorManagementPage)
  },
  {
    path: 'admin/patients',
    canActivate: [authGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () =>
      import('./features/admin/admin-patient-management.page').then((module) => module.AdminPatientManagementPage)
  },
  {
    path: '**',
    redirectTo: ''
  }
];
