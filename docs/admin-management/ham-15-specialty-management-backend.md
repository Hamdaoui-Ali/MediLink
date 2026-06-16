# HAM-15 Specialty Management Backend APIs

HAM-15 adds backend APIs for managing medical specialties. Specialties are used in doctor profiles and later patient search, so this issue creates the backend foundation that admin and doctor-management screens can build on.

## What Changed

- Added a `Specialty` JPA entity for the existing `specialties` table.
- Added `SpecialtyRepository` for specialty lookups, duplicate checks, and active-only listing.
- Added `SpecialtyService` for listing, creating, updating, activating, and deactivating specialties.
- Added `SpecialtyController` under `/api/v1/specialties`.
- Public specialty listing now returns active specialties by default so future doctor and patient flows do not accidentally use inactive specialties.
- Listing all specialties with inactive records requires an admin token.
- Added soft status changes instead of hard delete:
  - `ACTIVE`
  - `INACTIVE`
- Added `DELETE /api/v1/specialties/{id}` as a soft-delete alias that deactivates the specialty instead of removing the row.
- Added `SpecialtyService.getActiveSpecialty` so future doctor create/edit logic can require an active specialty before assigning it to a doctor profile.
- Added method-security protection so only admins can read individual management records, create, update, activate, deactivate, soft-delete, or list inactive specialties.
- Added explicit access-denied API error handling so method-security denials return `403` instead of falling into generic `500` handling.
- Added duplicate-name and not-found API error mappings.
- Added backend unit tests for service behavior, controller responses, admin protection annotations, and error handling.
- Added real backend integration tests that start the Spring application, run Flyway migrations, log in through the actual auth API, and exercise specialty APIs over HTTP.

The Notion connector was not available in this session, so HAM-15 was implemented from the Linear issue, existing repository rules, and the current MediLink backend architecture.

## API Contract

List specialties:

```http
GET /api/v1/specialties
GET /api/v1/specialties?activeOnly=true
```

This public list returns active specialties for doctor-profile and patient-search flows.

Admin list including inactive specialties:

```http
GET /api/v1/specialties?activeOnly=false
Authorization: Bearer <admin-token>
```

Create specialty:

```http
POST /api/v1/specialties
Content-Type: application/json
Authorization: Bearer <admin-token>
```

```json
{
  "name": "Cardiology",
  "description": "Heart and cardiovascular care"
}
```

Update specialty:

```http
PUT /api/v1/specialties/{id}
```

Activate or deactivate:

```http
PATCH /api/v1/specialties/{id}/activate
PATCH /api/v1/specialties/{id}/deactivate
DELETE /api/v1/specialties/{id}
```

`DELETE` performs a soft delete by setting status to `INACTIVE`.

Successful response data:

```json
{
  "id": 1,
  "name": "Cardiology",
  "description": "Heart and cardiovascular care",
  "status": "ACTIVE"
}
```

## Access Rules

The active list endpoint is available for frontend flows that need usable specialties. Admin-only management behavior is protected with method security:

- `GET /api/v1/specialties` is public and returns active specialties by default.
- `GET /api/v1/specialties?activeOnly=false` requires `ADMIN`.
- `GET /api/v1/specialties/{id}` requires `ADMIN`.
- `POST`, `PUT`, `PATCH`, and `DELETE` specialty management endpoints require `ADMIN`.
- Patient or anonymous users cannot modify specialties.

## Files

- `medilink-backend/src/main/java/com/medilink/medilink_backend/administration/domain` contains the specialty entity and status.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/administration/repository/SpecialtyRepository.java` contains persistence access.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/administration/service/SpecialtyService.java` owns specialty business behavior.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/administration/web/SpecialtyController.java` exposes the API.
- `medilink-backend/src/test/java/com/medilink/medilink_backend/administration/web/SpecialtyManagementIntegrationTest.java` verifies the real admin lifecycle, security restrictions, duplicate handling, JWT login, migrations, and persisted state through HTTP.
- `medilink-backend/src/test/java/com/medilink/medilink_backend/administration` contains the HAM-15 tests.

## Verification

After reworking HAM-15, backend tests were run with:

```powershell
.\mvnw.cmd test
```

Result: 53 tests passed, 0 failures, 0 errors.

The HAM-15-specific test coverage now includes:

- unit tests for specialty service functions, including active-specialty lookup for future doctor profile creation/editing;
- controller tests for response shaping and method-security annotations;
- real HTTP integration tests for admin create, update, list-all, activate, deactivate, and soft-delete;
- real HTTP integration tests proving patient and anonymous users cannot modify specialties;
- real HTTP integration tests proving duplicate specialty names return `409 CONFLICT`;
- application startup with local Flyway migrations and seeded admin login.

## Out Of Scope

HAM-15 does not build the Angular admin specialty management UI. That is handled by HAM-16.
