# HAM-32 + HAM-33 Patient Appointment List & Dashboard

HAM-32 (backend) and HAM-33 (frontend) allow patients to view their own upcoming and past appointments.

## HAM-32 Backend

The patient appointment list API was already implemented as part of HAM-30 (GET `/v1/patient/appointments` and GET `/v1/patient/appointments/{id}`). Appointments are scoped to the authenticated patient via JWT resolution.

## HAM-33 Frontend

- Created `PatientAppointmentsPage` at `/patient/appointments` with:
  - List of all patient appointments sorted newest first.
  - Each card shows: status badge (colored), date, doctor ID, time range, reason, and optional doctor notes.
  - "Cancel Appointment" button on CONFIRMED and RESCHEDULED appointments.
  - Loading, empty, and error states.
  - Success and error messages for cancellation actions.
- Added `/patient/appointments` route.
- Updated `PatientDashboardPage` with "My Appointments" navigation link.
