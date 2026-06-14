# HAM-34 Doctor Appointment Dashboard Backend APIs

HAM-34 implements the backend API that allows doctors to retrieve their appointments with support for filtering by status and date range. It ensures doctors can only access their own appointments and returns all required fields including patient name, appointment date/time, status, and reason.

## What Changed

### Backend

- Added `findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc` query method to `AppointmentRepository` for date range filtering.
- Added `findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc` query method for combined status + date range filtering.
- Added `listFilteredAppointments(Long doctorId, AppointmentStatus status, LocalDate from, LocalDate to)` method to `AppointmentService` that delegates to the appropriate repository query based on which filters are present (none, status only, date range only, or both).
- Updated `GET /v1/doctor/appointments` controller endpoint with two new optional query parameters: `from` (ISO date) and `to` (ISO date).
- Updated `AppointmentServiceTest` with 4 new tests: date range only, status + date range, empty results, no-filters delegation.
- Updated `DoctorAppointmentControllerTest` with 2 new tests: date range filter and combined status + date range filter.
- Updated frontend `AppointmentService` to support `from` and `to` query parameters.
- Updated `DoctorAppointmentsPage` to use server-side date filtering instead of client-side (passes date as both `from` and `to` for single-date queries).
- Updated frontend component and service tests to cover date range parameters.

## API Usage

### List Doctor Appointments

```
GET /v1/doctor/appointments
Authorization: Bearer <doctor-jwt>
```

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `status` | `AppointmentStatus` | No | Filter by appointment status (CONFIRMED, CANCELLED, COMPLETED, MISSED, RESCHEDULED) |
| `from` | ISO date (`yyyy-MM-dd`) | No | Filter appointments from this date (inclusive) |
| `to` | ISO date (`yyyy-MM-dd`) | No | Filter appointments up to this date (inclusive) |

#### Examples

```
# All appointments (no filters)
GET /v1/doctor/appointments

# Filter by status only
GET /v1/doctor/appointments?status=CONFIRMED

# Filter by date range only
GET /v1/doctor/appointments?from=2026-06-01&to=2026-06-30

# Filter by status and date range
GET /v1/doctor/appointments?status=CONFIRMED&from=2026-06-01&to=2026-06-30

# Filter for a specific date (same from and to)
GET /v1/doctor/appointments?from=2026-06-15&to=2026-06-15
```

#### Response

```json
{
  "success": true,
  "data": [
    {
      "id": 10,
      "doctorId": 1,
      "patientId": 2,
      "patientName": "Jane Patient",
      "appointmentDate": "2026-06-15",
      "startTime": "10:00:00",
      "endTime": "10:30:00",
      "status": "CONFIRMED",
      "reason": "Routine checkup",
      "doctorNotes": null
    }
  ],
  "timestamp": "2026-06-14T01:45:00Z"
}
```

## Filter Behavior

The `listFilteredAppointments` service method delegates to the appropriate Spring Data JPA query:

| Status | Date Range | Repository Method Used |
|--------|-----------|----------------------|
| null | null | `findByDoctorIdOrderByAppointmentDateDescStartTimeDesc` |
| set | null | `findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc` |
| null | set | `findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc` |
| set | set | `findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc` |

All results are sorted by `appointmentDate DESC, startTime DESC` (newest first).

## Patient Name Resolution

Patient names are resolved via a batch JPQL query that joins the `patients` and `users` tables:

```sql
SELECT new PatientRef(p.id, u.fullName)
FROM Patient p JOIN p.user u
WHERE p.id IN (:ids)
```

This avoids N+1 queries by collecting distinct patient IDs from the result set and fetching names in a single query.

## Access Control

- All endpoints require authentication with the `DOCTOR` role (class-level `@PreAuthorize("hasRole('DOCTOR')")`).
- Doctor identity is resolved from the JWT `userId` claim via the `doctors` table.
- All queries are scoped to `doctorId` -- a doctor can never see another doctor's appointments.
- Attempting to access another doctor's appointment by ID returns 404 Not Found.

## Test Coverage

### Backend (6 new tests)

| Test | What it covers |
|------|---------------|
| `listFilteredAppointmentsSupportsDateRangeOnly` | Doctor gets appointments within a date range |
| `listFilteredAppointmentsSupportsStatusAndDateRange` | Combined status + date range filtering |
| `listFilteredAppointmentsReturnsEmptyListWhenNoResults` | Empty result handling |
| `listFilteredAppointmentsNoFiltersDelegatesToListAll` | No-filter case delegates to correct repo method |
| `listAppointmentsSupportsDateRangeFilter` (controller) | Controller passes date params to service |
| `listAppointmentsSupportsStatusAndDateRangeFilter` (controller) | Controller passes both params to service |

### Frontend (3 new tests)

| Test | What it covers |
|------|---------------|
| `list doctor appointments filtered by date range` | Service builds query with `from` and `to` |
| `list doctor appointments filtered by status and date range` | Service builds query with all params |
| `filter appointments by date via server call` | Component passes date as `from`/`to` |

## Verification

- Backend: `mvnw.cmd test` -- 86 tests passed, 0 failures, 0 errors
- Frontend: `npm.cmd run build` -- build succeeds
- Frontend: `npm.cmd test -- --watch=false` -- 40 tests passed, 0 failures

## What Is Out Of Scope

- HAM-34 does not implement appointment creation or booking (HAM-30).
- HAM-34 does not implement patient appointment views (HAM-32).
- HAM-34 does not implement notes editing via the API (already covered by HAM-38).
- HAM-34 does not implement status updates via the API (already covered by HAM-38).
