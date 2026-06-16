# HAM-24 Blocked Slots Backend APIs

HAM-24 implements the backend API that allows doctors to create, list, update, and delete blocked periods when they are unavailable. Blocked slots prevent patients from booking during those times.

## What Changed

- Created `BlockedSlot` JPA entity mapping to the `blocked_slots` table with lifecycle hooks (`@PrePersist`, `@PreUpdate`) for automatic timestamp management.
- Created `BlockedSlotRepository` with query methods: `findByDoctorIdAndActiveTrueOrderByBlockDateDescStartTimeDesc`, `findByIdAndDoctorId`.
- Created `BlockedSlotService` with business logic:
  - `listBlockedSlots(doctorId)` — returns only active blocked slots sorted newest first.
  - `createBlockedSlot(doctorId, request)` — validates that endTime is after startTime, rejects past dates.
  - `updateBlockedSlot(doctorId, slotId, request)` — same validation as create, updates existing slot fields.
  - `deleteBlockedSlot(doctorId, slotId)` — soft-deletes by setting `is_active = false`.
  - `resolveDoctor(userId)` — resolves doctor identity from JWT userId via `DoctorRefRepository`.
- Created `DoctorBlockedSlotController` at `/v1/doctor/blocked-slots` with four endpoints, all protected with `@PreAuthorize("hasRole('DOCTOR')")`.
- Created custom exceptions (`BlockedSlotNotFoundException`, `InvalidBlockedSlotException`) with handlers in `GlobalExceptionHandler`.

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/doctor/blocked-slots` | DOCTOR | List own active blocked slots |
| POST | `/v1/doctor/blocked-slots` | DOCTOR | Create a new blocked slot |
| PATCH | `/v1/doctor/blocked-slots/{id}` | DOCTOR | Update an existing blocked slot |
| DELETE | `/v1/doctor/blocked-slots/{id}` | DOCTOR | Remove a blocked slot (soft delete) |

### Request / Response

**POST / PATCH** request body:
```json
{
  "blockDate": "2026-07-15",
  "startTime": "14:00:00",
  "endTime": "16:00:00",
  "reason": "Conference"
}
```

**GET / POST / PATCH** response:
```json
{
  "success": true,
  "data": {
    "id": 1,
    "doctorId": 1,
    "blockDate": "2026-07-15",
    "startTime": "14:00:00",
    "endTime": "16:00:00",
    "reason": "Conference"
  }
}
```

**DELETE** response: `204 No Content`

## Validation Rules

- `endTime` must be after `startTime` — returns 400 with `INVALID_BLOCKED_SLOT`.
- `blockDate` cannot be in the past — returns 400 with `INVALID_BLOCKED_SLOT`.
- `blockDate`, `startTime`, `endTime` are required (enforced by `@NotNull` on the DTO).
- Duplicate active slots for the same doctor+date+time range are prevented by a database unique constraint on `active_block_key`.

## Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/blockedslot/
  domain/
    BlockedSlot.java              # JPA entity for blocked_slots table
  repository/
    BlockedSlotRepository.java    # JPA repo: findByDoctorIdAndActiveTrue, findByIdAndDoctorId
  service/
    BlockedSlotService.java       # Business logic + validation
    BlockedSlotNotFoundException.java
    InvalidBlockedSlotException.java
  web/
    DoctorBlockedSlotController.java  # REST controller at /v1/doctor/blocked-slots
    BlockedSlotResponse.java      # Response DTO
    BlockedSlotRequest.java       # Request DTO with @NotNull validation
```

## Access Control

- All endpoints require the `DOCTOR` role via `@PreAuthorize("hasRole('DOCTOR')")`.
- Doctor identity is resolved from the JWT `userId` claim via `doctors` table lookup.
- All queries and mutations are scoped to the authenticated doctor's `doctorId`.
- Accessing or modifying another doctor's blocked slot returns 404.

## Test Coverage

| Test File | Tests | Type | What it covers |
|-----------|-------|------|---------------|
| `BlockedSlotServiceTest` | 11 | Unit | Doctor resolution, list/create/update/delete success and failure paths |
| `DoctorBlockedSlotControllerTest` | 5 | Unit | Controller delegates to service, @PreAuthorize check |
| `DoctorBlockedSlotIntegrationTest` | 7 | Integration | Full HTTP: list, create, update, delete, cross-doctor, invalid time, unauthenticated |

## Verification

- Backend: `mvnw.cmd test` — 112 tests passed, 0 failures, 0 errors

## What Is Out Of Scope

- HAM-24 does not build the frontend blocked slots UI (covered by HAM-25).
- HAM-24 does not integrate blocked slots with the booking flow to prevent overlapping appointments.
- The `active_block_key` unique constraint prevents exact duplicate active slots, but the backend does not currently validate overlapping time ranges within a single day beyond what the database constraint enforces.
