# HAM-35 Doctor Appointment Dashboard UI

HAM-35 builds the doctor appointment dashboard frontend page so doctors can view, filter, and manage their confirmed appointments. It also adds patient name resolution to the backend API.

## What Changed

### Frontend

- Created `Appointment` model and `AppointmentStatus` type in `shared/models/appointment.model.ts`.
- Created `AppointmentService` in `shared/services/appointment.service.ts` that calls `GET /v1/doctor/appointments` with optional status filter.
- Created `DoctorAppointmentsPage` component at `features/doctor/doctor-appointments.page.ts` with:
  - Status filter dropdown (All, Confirmed, Cancelled, Completed, Missed, Rescheduled).
  - Date filter input for client-side date filtering.
  - Table displaying patient name, appointment date, time range, reason, and status badge.
  - Loading state ("Loading appointments...") during data fetch.
  - Empty state ("No appointments found." / "No appointments match your filters.") when no data or filter matches.
  - Error state with role-specific messages (403: permission, 401: session expired, generic fallback).
  - Status pills with color-coded badges (blue for Confirmed, green for Completed, gray for Cancelled, amber for Missed, purple for Rescheduled).
  - Refresh button and clear filters button.
- Added `/doctor/appointments` route in `app.routes.ts` protected by `authGuard` with `DOCTOR` role.
- Updated `DoctorDashboardPage` to include a navigation link to the appointments page.

### Backend

- Added `PatientRef` record (`id`, `fullName`) to `appointment/domain/PatientRef.java`.
- Added `patientName` field to `AppointmentResponse` DTO.
- Added `findPatientNamesByIds()` JPQL query to `PatientRepository` that joins patients with users.
- Updated `AppointmentService` to batch-resolve patient names from patient IDs and populate `patientName` in all appointment responses.
- Updated `AppointmentService` to inject `PatientRepository`.
- Updated existing backend tests to include `patientName` in response assertions.

## Frontend Architecture

```
medilink-frontend/src/app/
  features/doctor/
    doctor-appointments.page.ts      # Component: signals for state, Filters via FormsModule
    doctor-appointments.page.html    # Template: filters bar + appointment table + states
    doctor-appointments.page.scss    # Styles: responsive, status pills, panel layout
    doctor-appointments.page.spec.ts # 13 tests covering loading, errors, filters, display
    doctor-dashboard.page.ts         # Updated with RouterLink to appointments
  shared/
    models/
      appointment.model.ts           # Appointment interface, AppointmentStatus type, ALL_STATUSES
    services/
      appointment.service.ts         # Calls GET /v1/doctor/appointments[?status=]
      appointment.service.spec.ts    # 3 tests: list, filter, envelope handling
```

## API Used

| Method | Path | Description |
|--------|------|-------------|
| GET | `/v1/doctor/appointments` | List all doctor appointments (newest first) |
| GET | `/v1/doctor/appointments?status=CONFIRMED` | Filter by status |

The UI sends the status filter as a query parameter to the server. The date filter is applied client-side on the returned results.

## Filter Behavior

- **Status filter**: Sent as `?status=` query parameter to the backend. Changing the dropdown triggers a new API call.
- **Date filter**: Sent as `?from=<date>&to=<date>` query parameters to the backend. Changing the date input triggers a new API call fetching appointments for that specific date.
- **Clear filters**: Resets both filters to empty and reloads all appointments from the API.

## Patient Name Resolution

The appointment API (`/v1/doctor/appointments`) now returns `patientName` in each appointment response. The backend resolves this by joining the `patients` and `users` tables via a JPQL query that maps `patient.id -> user.full_name`. The batch lookup avoids N+1 queries by collecting all distinct patient IDs and fetching names in a single query.

## Test Coverage

### Frontend (16 new tests)

| Test File | Tests | What it covers |
|-----------|-------|---------------|
| `appointment.service.spec.ts` | 3 | List all, filter by status, envelope handling |
| `doctor-appointments.page.spec.ts` | 13 | Init load, loading state, permission error (403), auth error (401), empty state, status filter change, date filter (client-side), clear filters, time formatting, date formatting, status labels, status CSS classes, filter no-match message |

### Backend (updated existing)

- `AppointmentServiceTest`: Updated to mock `PatientRepository` and verify `patientName` in list results.
- `DoctorAppointmentControllerTest`: Updated `AppointmentResponse` constructor calls with `patientName`.

## Verification

- Backend: `mvnw.cmd test` — 86 tests passed, 0 failures, 0 errors
- Frontend: `npm.cmd run build` — build succeeds
- Frontend: `npm.cmd test -- --watch=false` — 40 tests passed, 0 failures

## Access Rules

- Only authenticated users with `DOCTOR` role can access `/doctor/appointments`.
- The backend enforces doctor ownership — a doctor only sees their own appointments.
- Unauthenticated requests receive 401. Non-doctor authenticated requests receive 403.

## What Is Out Of Scope

- HAM-35 does not build the appointment detail view (single appointment drill-down).
- HAM-35 does not implement appointment notes editing (covered by HAM-39).
- HAM-35 does not implement appointment status updates from the UI (covered by HAM-39).
- HAM-35 does not implement appointment creation or booking (HAM-30).
- HAM-35 does not implement cancellation or rescheduling UI (HAM-36/37).
