# HAM-30 Automatic Appointment Booking Backend API

HAM-30 implements the backend API that allows patients to book appointments with doctors. It creates new appointment records, validates slot availability, and prevents double-booking through overlapping time range checks and database-level unique constraints.

## What Changed

- Created `PatientRefEntity` JPA entity mapping to the `patients` table for resolving patient identity from JWT claims.
- Created `PatientRefRepository` with `findByUserId` query for patient JWT resolution.
- Created `PatientAppointmentController` at `/v1/patient/appointments` with three endpoints, all protected with `@PreAuthorize("hasRole('PATIENT')")`.
- Added booking business logic to `AppointmentService`:
  - `resolvePatient(userId)` — resolves patient identity from JWT userId via `PatientRefRepository`.
  - `createAppointment(patientId, doctorId, date, time, reason)` — validates the slot is in the future, checks doctor exists and is active, computes end time from consultation duration, checks for overlapping confirmed/rescheduled appointments, creates and saves the appointment with `CONFIRMED` status.
  - `listPatientAppointments(patientId)` — returns all appointments for the authenticated patient sorted newest first.
  - `getPatientAppointment(patientId, appointmentId)` — returns a single appointment scoped to the patient.
- Added lifecycle hooks (`@PrePersist`, `@PreUpdate`) to the `Appointment` entity for automatic timestamp management, matching the pattern used by `BlockedSlot`.
- Added a constructor to `Appointment` for creating new bookings with `CONFIRMED` status.
- Added `consultationDurationMinutes` field to `DoctorRef` entity and a `findConsultationDurationById` query to `DoctorRefRepository` so the booking logic can compute the appointment end time.
- Added patient-scoped and conflict-check queries to `AppointmentRepository`:
  - `findByPatientIdOrderByAppointmentDateDescStartTimeDesc` — patient appointment history.
  - `findByDoctorIdAndAppointmentDateAndStatusIn` — conflict detection query that retrieves active appointments (CONFIRMED, RESCHEDULED) for a doctor on a specific date.
- Created custom exceptions `PatientNotFoundException` and `SlotUnavailableException` with handlers in `GlobalExceptionHandler`.

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| POST | `/v1/patient/appointments` | PATIENT | Book a new appointment |
| GET | `/v1/patient/appointments` | PATIENT | List own appointments |
| GET | `/v1/patient/appointments/{id}` | PATIENT | Get a single appointment |

### Request / Response

**POST** request body:
```json
{
  "doctorId": 5,
  "appointmentDate": "2026-08-01",
  "startTime": "09:00:00",
  "reason": "General consultation"
}
```

**POST / GET** response:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "doctorId": 5,
    "patientId": 3,
    "patientName": "John Doe",
    "appointmentDate": "2026-08-01",
    "startTime": "09:00:00",
    "endTime": "09:30:00",
    "status": "CONFIRMED",
    "reason": "General consultation",
    "doctorNotes": null
  }
}
```

## Validation Rules

- `appointmentDate` must be today or in the future — returns 409 with `SLOT_UNAVAILABLE`.
- `startTime` cannot be in the past for today's date — returns 409 with `SLOT_UNAVAILABLE`.
- Doctor must exist and be active — returns 404 `DOCTOR_NOT_FOUND` or 409 `SLOT_UNAVAILABLE`.
- Slot cannot overlap with an existing confirmed or rescheduled appointment for the same doctor on the same date — returns 409 with `SLOT_UNAVAILABLE`.
- `doctorId`, `appointmentDate`, `startTime`, and `reason` are required (enforced by `@NotNull`/`@NotBlank` on the DTO).
- Appointment end time is computed as `startTime + consultation_duration_minutes` (defaults to 30 minutes).
- Double-booking prevention is supported at both the application level (overlap check) and database level (unique constraint on `active_slot_key`).

## Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/appointment/
  domain/
    PatientRefEntity.java           # JPA entity for patients table
    Appointment.java                # Updated with constructor + @PrePersist/@PreUpdate
    DoctorRef.java                  # Updated with consultationDurationMinutes
  repository/
    PatientRefRepository.java       # JPA repo: findByUserId
    AppointmentRepository.java      # Updated with patient + conflict queries
    DoctorRefRepository.java        # Updated with findConsultationDurationById
  service/
    AppointmentService.java         # resolvePatient, createAppointment, listPatientAppointments, getPatientAppointment
    PatientNotFoundException.java   # 404 when patient not found
    SlotUnavailableException.java   # 409 when slot is taken
  web/
    PatientAppointmentController.java  # REST controller at /v1/patient/appointments
    BookAppointmentRequest.java     # Request DTO with @NotNull/@NotBlank validation
```

## Access Control

- All endpoints require the `PATIENT` role via `@PreAuthorize("hasRole('PATIENT')")`.
- Patient identity is resolved from the JWT `userId` claim via `patients` table lookup.
- All queries and mutations are scoped to the authenticated patient's `patientId`.
- Accessing another patient's appointment returns 404.

## Test Coverage

| Test File | Tests | Type | What it covers |
|-----------|-------|------|---------------|
| `AppointmentServiceTest` | 19 | Unit | Existing doctor tests + new resolvePatient test support |
| `DoctorAppointmentControllerTest` | 6 | Unit | Existing controller tests unchanged |
| `DoctorAppointmentIntegrationTest` | 11 | Integration | Existing doctor integration tests unchanged |
| `PatientAppointmentIntegrationTest` | 10 | Integration | Book appointment, slot conflict, past date, invalid doctor, list own appointments, get single appointment, unauthenticated access, cross-role access (doctor as patient), overlapping time slot, missing required fields |

## What Is Out Of Scope

- HAM-30 does not implement the slot generation API that returns available time slots (covered by HAM-28).
- HAM-30 does not send email confirmations or notifications (covered by a future notification milestone).
- HAM-30 does not handle appointment cancellation or rescheduling (covered by HAM-36).
- HAM-30 does not validate that the appointment time aligns with the doctor's availability schedule (the `doctor_availability` table is not yet wired into the booking flow).
