# HAM-38 Doctor Appointment Notes and Status Update Backend APIs

HAM-38 implements backend APIs that let doctors add private notes and update appointment status after patient visits. Doctors can view their appointments, mark them as Completed/Cancelled/Missed/Rescheduled, and add clinical notes that are invisible to patients.

## What Changed

- Created `Appointment` JPA entity mapping to the `appointments` table with all fields including `doctor_notes`.
- Created `AppointmentStatus` enum with valid transition rules: CONFIRMED can go to COMPLETED, CANCELLED, MISSED, or RESCHEDULED; RESCHEDULED can go back to CONFIRMED or to COMPLETED/CANCELLED/MISSED. Terminal states (COMPLETED, CANCELLED, MISSED) block further transitions.
- Created `DoctorRef` entity as a lightweight read-only mapping to the `doctors` table for resolving doctor identity from JWT userId claim.
- Built `AppointmentService` with business logic for listing, viewing, updating notes, and updating status with transition validation.
- Created `DoctorAppointmentController` at `/v1/doctor/appointments` with four endpoints, all protected with `@PreAuthorize("hasRole('DOCTOR')")`.
- Added custom exceptions (`AppointmentNotFoundException`, `DoctorRefNotFoundException`, `InvalidAppointmentStatusException`) with handlers in `GlobalExceptionHandler`.

## Backend Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/appointment/
  domain/
    Appointment.java               # JPA entity for appointments table
    AppointmentStatus.java         # Enum: CONFIRMED, CANCELLED, COMPLETED, MISSED, RESCHEDULED
    DoctorRef.java                 # Lightweight read-only doctor lookup entity
  repository/
    AppointmentRepository.java     # JPA repo: findByDoctorId, findByIdAndDoctorId
    DoctorRefRepository.java       # JPA repo: findByUserId
  service/
    AppointmentService.java        # Business logic + status transition validation
    AppointmentNotFoundException.java
    DoctorRefNotFoundException.java
    InvalidAppointmentStatusException.java
  web/
    DoctorAppointmentController.java # REST controller at /v1/doctor/appointments
    AppointmentResponse.java       # Response DTO
    UpdateNotesRequest.java        # Request DTO for PATCH notes
    UpdateStatusRequest.java       # Request DTO for PATCH status
```

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/doctor/appointments` | DOCTOR | List own appointments (optionally filter by status) |
| GET | `/v1/doctor/appointments/{id}` | DOCTOR | View a single appointment |
| PATCH | `/v1/doctor/appointments/{id}/notes` | DOCTOR | Add or update private doctor notes |
| PATCH | `/v1/doctor/appointments/{id}/status` | DOCTOR | Update appointment status |

All endpoints extract the doctor's identity from the JWT `userId` claim, resolve it to a `doctor_id` via the `doctors` table, and enforce ownership. A doctor cannot access or modify another doctor's appointments.

## Status Transition Rules

| From | Allowed To |
|------|-----------|
| CONFIRMED | COMPLETED, CANCELLED, MISSED, RESCHEDULED |
| RESCHEDULED | CONFIRMED, COMPLETED, CANCELLED, MISSED |
| COMPLETED | (none - terminal) |
| CANCELLED | (none - terminal) |
| MISSED | (none - terminal) |

Invalid transitions return HTTP 400 with error code `INVALID_STATUS_TRANSITION` and a descriptive message.

## Access Rules

- Only authenticated users with `DOCTOR` role can access the endpoints.
- Class-level `@PreAuthorize("hasRole('DOCTOR')")` enforces this uniformly.
- Doctor ownership is validated on every request via `findByIdAndDoctorId`. Accessing another doctor's appointment returns 404.
- Unauthenticated requests receive 401 UNAUTHORIZED.
- Non-doctor authenticated requests receive 403 FORBIDDEN.
- Patient requests to the doctor appointment endpoints return 403 FORBIDDEN.

## Private Notes

Doctor notes are stored in the `doctor_notes` TEXT column of the `appointments` table. The API only allows doctors to read and write these notes (via the PATCH endpoint). Since there is no patient appointment endpoint yet (HAM-32), patient access to notes is implicitly prevented. When patient appointment endpoints are added, they must explicitly exclude the `doctorNotes` field from responses.

## Test Coverage

| Test | Type | What it covers |
|------|------|---------------|
| `AppointmentServiceTest` | Unit (15 tests) | Resolve doctor (active, not found, inactive), get appointment (own, not found), update notes (success, not found), update status (all valid transitions, invalid transition, terminal state rejection), list appointments, terminal state check |
| `DoctorAppointmentControllerTest` | Unit (4 tests) | List returns appointments, update notes saves, update status changes, class-level @PreAuthorize |
| `DoctorAppointmentIntegrationTest` | Integration (8 tests) | List own appointments, view single, update notes, mark completed, cross-doctor access blocked, invalid transition 400, unauth 401, patient 403 |

## Verification

- Backend: `mvnw.cmd test` — 80 tests passed, 0 failures, 0 errors

## What Is Out Of Scope

- HAM-38 does not build the frontend appointment notes/status UI. That is covered by HAM-39 ("Build doctor appointment notes and status UI").
- HAM-38 does not implement patient appointment views (HAM-32).
- HAM-38 does not implement appointment creation or booking (HAM-30).
- HAM-38 does not implement cancellation/rescheduling UI (HAM-36/37).
- The `DoctorRef` entity uses a different class name from the `Doctor` entity in HAM-17/23 to avoid merge conflicts. When merging, the user should pick one Doctor entity.
