# HAM-23 Doctor Weekly Availability UI

HAM-23 builds the doctor-facing interface for managing weekly availability. Doctors can set recurring weekly schedules through a UI that handles add, edit, and remove operations, with time validation and grouped day views.

This issue also implements the backend APIs required by the UI (originally planned as HAM-22), since HAM-22 was not yet implemented. The combined implementation covers both the backend availability service and the frontend availability page.

## What Changed

### Backend

- Created `Doctor` JPA entity with `DoctorStatus` enum in the `doctor` package. This is a minimal entity that maps to the existing `doctors` table, using `findByUserId` to resolve the logged-in user to their doctor record. A full Doctor entity exists in the unmerged HAM-17 branch and can replace this minimal version later.
- Created `DoctorAvailability` JPA entity mapping to the `doctor_availability` table with fields for `doctorId`, `dayOfWeek` (1-7), `startTime`, `endTime`, and `isActive`.
- Built `DoctorAvailabilityRepository` with methods for listing active slots by doctor, secure lookup by ID + doctor ID, and existence checks.
- Built `DoctorAvailabilityService` with business logic for listing, adding, updating, and deactivating availability slots. Validates that end time is after start time and that time strings are parseable. Enforces doctor-ownership on all operations (a doctor cannot access another doctor's slots).
- Created `DoctorAvailabilityController` at `GET/POST /v1/doctor/availability` and `PUT/DELETE /v1/doctor/availability/{id}`, all protected with `@PreAuthorize("hasRole('DOCTOR')")`. The controller extracts the `userId` from the JWT, resolves it to a `doctorId`, and delegates to the service.
- Added custom exceptions (`DoctorNotFoundException`, `AvailabilityNotFoundException`, `InvalidAvailabilityException`) with handlers in `GlobalExceptionHandler` mapped to HTTP 404 and 400.
- Added `delete` method to `ApiService` in the frontend.

### Frontend

- Created `DoctorAvailability` and `DoctorAvailabilityRequest` TypeScript models with `DAY_NAMES` mapping for displaying day labels.
- Created `DoctorAvailabilityService` wrapping the four CRUD endpoints (list, add, update, remove).
- Built `DoctorAvailabilityPage` component with:
  - A form card for adding/editing availability (day dropdown, start time, end time)
  - Toggle between add and edit mode, with cancel support
  - Grouped table view of active slots organized by day of week
  - Inline edit and remove actions per slot
  - Duration display for each slot (computed from start/end times)
  - Loading state with spinner text
  - Error state with retry button
  - Empty state with instructional message
  - Success and error feedback banners
- Updated `DoctorDashboardPage` with a styled navigation card linking to `/doctor/availability`.
- Registered route `/doctor/availability` with admin-only (`DOCTOR`) role guard in `app.routes.ts`.

## Backend Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/doctor/
  domain/
    Doctor.java                       # JPA entity for doctors table
    DoctorStatus.java                 # ACTIVE, INACTIVE enum
    DoctorAvailability.java           # JPA entity for doctor_availability table
  repository/
    DoctorRepository.java             # JPA repo with findByUserId
    DoctorAvailabilityRepository.java # JPA repo with findByIdAndDoctorId, etc.
  service/
    DoctorAvailabilityService.java    # Business logic + validation
    DoctorNotFoundException.java      # 404 when doctor not found
    AvailabilityNotFoundException.java # 404 when slot not found
    InvalidAvailabilityException.java # 400 for invalid time ranges/formats
  web/
    DoctorAvailabilityController.java # REST controller at /v1/doctor/availability
    DoctorAvailabilityRequest.java    # dayOfWeek, startTime, endTime
    DoctorAvailabilityResponse.java   # id, dayOfWeek, startTime, endTime, isActive
```

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/doctor/availability` | DOCTOR | List active availability slots |
| POST | `/v1/doctor/availability` | DOCTOR | Add a new availability slot |
| PUT | `/v1/doctor/availability/{id}` | DOCTOR | Update an existing slot |
| DELETE | `/v1/doctor/availability/{id}` | DOCTOR | Deactivate (soft delete) a slot |

All endpoints extract the doctor's identity from the JWT `userId` claim and resolve it to the `doctor_id`. Cross-doctor access returns 404.

## UI Features

- **Add/Edit form**: Shared form with dropdown for day of week (Monday-Sunday), time inputs for start and end, and a submit button that switches label between "Add Slot" and "Save Changes" based on mode.
- **Grouped slot list**: Active slots are grouped by day of week with a blue header bar. Each group shows a table with start time, end time, computed duration, and action buttons.
- **Edit mode**: Clicking "Edit" on a slot pre-fills the form with its values and switches to edit mode. A "Cancel" button resets the form to add mode.
- **Remove action**: Clicking "Remove" immediately sends the DELETE request and removes the slot from the local list. A success message confirms the action.
- **Validation**: The form prevents submission when empty or invalid. The backend rejects invalid time ranges (end <= start) with clear error messages.
- **Duration display**: Each slot shows its computed duration (e.g., "8h", "4h 30m").

## Access Rules

- Only authenticated users with `DOCTOR` role can access availability endpoints.
- The class-level `@PreAuthorize("hasRole('DOCTOR')")` enforces this on all controller methods.
- The frontend route is protected by `authGuard` with `data: { roles: ['DOCTOR'] }`.
- A doctor can only manage their own availability. Accessing another doctor's slot returns 404.
- Unauthenticated requests receive 401 UNAUTHORIZED.
- Non-doctor authenticated requests (admin, patient) receive 403 FORBIDDEN.

## Test Coverage

### Backend

| Test | Type | What it covers |
|------|------|---------------|
| `DoctorAvailabilityServiceTest` | Unit (13 tests) | Resolve doctor (active, inactive, not found), list availability, add (valid time, invalid time range, same start/end, invalid format), update, update not found, deactivate, deactivate not found, empty list |
| `DoctorAvailabilityControllerTest` | Unit (4 tests) | List returns slots, add returns 201, delete deactivates, class-level @PreAuthorize |
| `DoctorAvailabilityIntegrationTest` | Integration (5 tests) | Unauthorized 401, patient 403, full doctor lifecycle (add/list/update/deactivate), cross-doctor access blocked, invalid time range 400 |

### Frontend

| Test | Type | What it covers |
|------|------|---------------|
| `doctor-availability.service.spec.ts` | Unit (4 tests) | List GET, add POST, update PUT, remove DELETE |
| `doctor-availability.page.spec.ts` | Component (9 tests) | Load on init, grouped display, empty state, error state, add slot, remove slot, edit mode, cancel edit, prevent invalid submit |

## Verification

- Backend: `mvnw.cmd test` — 86 tests passed, 0 failures, 0 errors (22 new tests)
- Frontend tests: `npm.cmd test -- --watch=false` — 12 test files, 45 tests passed (13 new tests)
- Frontend build: `npm.cmd run build` — production build completed successfully

## What Is Out Of Scope

- HAM-23 does not implement the full doctor profile management (that is HAM-17, currently on an unmerged branch).
- HAM-23 does not implement the blocked slots feature (HAM-24/25).
- HAM-23 does not connect availability to appointment slot generation (HAM-28).
- HAM-23 does not implement overlapping slot detection beyond the database unique constraint.
- The minimal `Doctor` entity created here should be replaced with the full entity from HAM-17 when that branch is merged.
