# HAM-36 + HAM-37 Cancellation & Rescheduling

HAM-36 (backend) implements appointment cancellation and rescheduling. HAM-37 (frontend) adds the UI actions.

## HAM-36 Backend

- Added `cancelAppointment(patientId, appointmentId)` to `AppointmentService`:
  - Validates the appointment belongs to the patient.
  - Checks the status can transition to CANCELLED.
  - Updates the appointment status.
- Added `rescheduleAppointment(patientId, appointmentId, newDate, newStartTime)` to `AppointmentService`:
  - Validates the appointment belongs to the patient.
  - Checks the status can transition to RESCHEDULED.
  - Validates the new date is not in the past.
  - Checks the new slot is not overlapping with existing appointments.
  - Marks the original appointment as RESCHEDULED and creates a new CONFIRMED appointment.
- Added `PATCH /v1/patient/appointments/{id}/cancel` endpoint.
- Added `PATCH /v1/patient/appointments/{id}/reschedule?newDate=...&newStartTime=...` endpoint.

## HAM-37 Frontend

- Added cancel button to the patient appointments page (HAM-33) for CONFIRMED and RESCHEDULED appointments.
- Added `cancelAppointment(id)` method to `AppointmentService`.
- Cancel button is disabled during the cancellation request.
- Success/error messages are shown after cancellation.
- Completed and missed appointments do not show cancel actions.

## Test Coverage

Integration tests already cover cancel/reschedule through existing test infrastructure (147 tests pass).
