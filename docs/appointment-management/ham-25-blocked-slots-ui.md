# HAM-25 Blocked Slots UI for Doctors

HAM-25 builds the full blocked slots feature for doctors — a backend API and a frontend page that lets doctors block off unavailable time periods (vacation, meetings, etc.) so patients cannot book during those times.

## What Changed

### Backend

- Created `BlockedSlot` JPA entity mapping to the `blocked_slots` table with lifecycle hooks (`@PrePersist`, `@PreUpdate`) for `created_at` and `updated_at` timestamps.
- Created `BlockedSlotRepository` with query methods: `findByDoctorIdAndActiveTrueOrderByBlockDateDescStartTimeDesc`, `findByIdAndDoctorId`.
- Created `BlockedSlotService` with business logic:
  - `listBlockedSlots(doctorId)` — returns active blocked slots for the doctor.
  - `createBlockedSlot(doctorId, request)` — validates endTime > startTime, blocks past dates, saves new slot.
  - `deleteBlockedSlot(doctorId, slotId)` — soft-deletes by setting `is_active = false`.
  - `resolveDoctor(userId)` — resolves doctor identity from JWT via `DoctorRefRepository`.
- Created `DoctorBlockedSlotController` at `/v1/doctor/blocked-slots` with three endpoints, all protected with `@PreAuthorize("hasRole('DOCTOR')")`.
- Created custom exceptions (`BlockedSlotNotFoundException`, `InvalidBlockedSlotException`) with handlers in `GlobalExceptionHandler`.

### Frontend

- Created `BlockedSlot` and `BlockedSlotRequest` models in `shared/models/blocked-slot.model.ts`.
- Created `BlockedSlotService` in `shared/services/blocked-slot.service.ts` with list, create, and delete methods.
- Created `DoctorBlockedSlotsPage` component with:
  - Form for creating blocked slots (date with `min` set to today, start time, end time, optional reason).
  - Validation: required date/start/end time, form-level error messages.
  - Table showing existing blocked slots (date, time range, reason) with Remove button.
  - Loading, empty, error, and success states.
  - Refresh button to reload the list.
- Added `delete` method to `ApiService` for HTTP DELETE support.
- Added `/doctor/blocked-slots` route in `app.routes.ts`.
- Updated `DoctorDashboardPage` with navigation link to blocked slots page.

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/doctor/blocked-slots` | DOCTOR | List own active blocked slots |
| POST | `/v1/doctor/blocked-slots` | DOCTOR | Create a new blocked slot |
| DELETE | `/v1/doctor/blocked-slots/{id}` | DOCTOR | Remove a blocked slot (soft delete) |

### Request Body (POST)

```json
{
  "blockDate": "2026-07-15",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "reason": "Conference"
}
```

### Response (GET/POST)

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "doctorId": 1,
      "blockDate": "2026-07-15",
      "startTime": "14:00:00",
      "endTime": "16:00:00",
      "reason": "Conference"
    }
  ]
}
```

## Validation Rules

- `endTime` must be after `startTime` — returns 400 with `INVALID_BLOCKED_SLOT` error code.
- `blockDate` cannot be in the past — returns 400 with `INVALID_BLOCKED_SLOT` error code.
- `blockDate`, `startTime`, `endTime` are required (validated both by `@NotNull` on the DTO and Angular form validators).
- Duplicate active slots for the same doctor+date+time are prevented by a database unique constraint on `active_block_key`.

## Access Control

- All endpoints require `DOCTOR` role via `@PreAuthorize("hasRole('DOCTOR')")`.
- Doctor identity is resolved from JWT `userId` claim via `DoctorRefRepository`.
- All queries and mutations are scoped to the authenticated doctor's ID.
- Attempting to delete another doctor's blocked slot returns 404.

## Test Coverage

### Backend (18 tests)

| Test File | Tests | What it covers |
|-----------|-------|---------------|
| `BlockedSlotServiceTest` | 8 | Doctor resolution (active, not found), list slots, create valid slot, create reject end<start, create reject past date, delete deactivates, delete not found |
| `DoctorBlockedSlotControllerTest` | 4 | List returns slots, create saves, delete removes, class-level @PreAuthorize |
| `DoctorBlockedSlotIntegrationTest` | 6 | List own slots, create slot, delete slot (soft delete verified in DB), cross-doctor delete blocked, invalid time range rejected, unauthenticated 401 |

### Frontend (12 tests)

| Test File | Tests | What it covers |
|-----------|-------|---------------|
| `blocked-slot.service.spec.ts` | 3 | List, create, delete HTTP calls |
| `doctor-blocked-slots.page.spec.ts` | 9 | Init load, permission error (403), create adds to list, invalid form rejected, delete removes from list, validation errors, time formatting, date formatting, min date |

## Verification

- Backend: `mvnw.cmd test` — 107 tests passed, 0 failures
- Frontend: `npm.cmd run build` — build succeeds
- Frontend: `npm.cmd test -- --watch=false` — 52 tests passed, 0 failures

## What Is Out Of Scope

- HAM-25 does not implement weekly availability management (HAM-22/23).
- HAM-25 does not integrate blocked slots with the booking flow to prevent overlapping appointments.
- HAM-25 does not implement editing existing blocked slots (only create and delete).
