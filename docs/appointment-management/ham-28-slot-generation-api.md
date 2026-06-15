# HAM-28 Available Appointment Slot Generation API

HAM-28 implements the backend API that generates available time slots for a doctor on a given date by combining availability schedules, existing appointments, and blocked slots.

## What Changed

- Created `DoctorAvailability` JPA entity mapping to the `doctor_availability` table with `@PrePersist`/`@PreUpdate` lifecycle hooks.
- Created `DoctorAvailabilityRepository` with `findByDoctorIdAndDayOfWeekAndActiveTrue` query.
- Created `DoctorAvailabilityService` with `getAvailableSlots(doctorId, date)`:
  - Looks up the doctor's availability windows for the given day of week.
  - Generates time slots based on the doctor's `consultation_duration_minutes`.
  - Excludes slots that overlap with active blocked slots for that date.
  - Excludes slots that overlap with existing confirmed/rescheduled appointments.
  - Excludes past time slots for today's date.
- Created `DoctorAvailabilityController` at `GET /v1/patient/doctors/{doctorId}/slots?date=YYYY-MM-DD` (PATIENT role).
- Added `findByDoctorIdAndBlockDateAndActiveTrue` query to `BlockedSlotRepository` for date-scoped blocked slot queries.
- Created `SlotResponse` DTO record with `startTime` and `endTime` fields.

## API Endpoint

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/patient/doctors/{doctorId}/slots?date=YYYY-MM-DD` | PATIENT | Get available time slots for a doctor on a date |

## Test Coverage

| Test | Tests | Type |
|------|-------|------|
| `DoctorAvailabilityIntegrationTest` | 6 | Integration |
