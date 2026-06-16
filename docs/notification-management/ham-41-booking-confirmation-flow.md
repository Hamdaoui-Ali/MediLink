# HAM-41 Booking Confirmation Flow

HAM-41 implements automatic notification record creation when a patient books an appointment. When a booking succeeds, the system creates an `APPOINTMENT_CONFIRMATION` notification tracking record linked to the booking. The booking remains stable even if notification creation fails.

## What Changed

- Updated `AppointmentService.createAppointment()` to create a confirmation notification after a successful booking:
  - Resolves the patient's `user_id` from the `patients` table via `PatientRefRepository`.
  - Creates a `Notification` record with type `APPOINTMENT_CONFIRMATION` and status `PENDING`.
  - Links the notification to the appointment by `appointment_id`.
  - Wrapped in a `try-catch` so booking succeeds even if notification creation fails (e.g., database error, patient ref lookup failure).
- Injected `NotificationRepository` into `AppointmentService`.
- Added 3 unit tests covering:
  - Notification creation succeeds after booking.
  - Booking succeeds even when notification creation throws an exception.
  - Booking succeeds when patient reference lookup fails (returns empty).
- Updated existing `AppointmentServiceTest` to mock the new `NotificationRepository` dependency.

## Architecture

The confirmation flow is an internal concern of `AppointmentService`:
```
PatientAppointmentController (POST /v1/patient/appointments)
  → AppointmentService.createAppointment()
    → Validate slot + doctor
    → Save Appointment entity
    → createConfirmationNotification()  ← HAM-41
      → Resolve patient user_id
      → Save Notification (APPOINTMENT_CONFIRMATION, PENDING)
      → Catch + log any errors (booking NOT rolled back)
```

## Resilience

If notification creation fails for any reason:
- The booking is already persisted and returned successfully.
- The error is logged at `ERROR` level.
- No exception propagates to the caller.
- The patient is not affected — the appointment is still booked.

## Test Coverage

| Test | Tests | Type |
|------|-------|------|
| `AppointmentServiceTest` (existing + new) | 22 | Unit |
| `PatientAppointmentIntegrationTest` | 10 | Integration (existing) |

New tests specifically for HAM-41:
1. Booking creates a confirmation notification
2. Booking succeeds when notification creation throws an exception
3. Booking succeeds when patient ref lookup returns empty

## What Is Out Of Scope

- HAM-41 does not send actual emails (covered by email sending issues).
- HAM-41 only creates records for new bookings, not for cancellations or reschedules.
- HAM-41 uses a placeholder email address; real recipient emails will be wired in a later issue.
