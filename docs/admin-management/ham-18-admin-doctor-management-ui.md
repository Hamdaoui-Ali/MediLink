# HAM-18 - Admin Doctor Management UI

## Scope

Implemented the admin-facing doctor management screen for milestone 03, using the backend contract planned for HAM-17.

Notion context was requested by the project rules, but the Notion connector is not available in this Codex session. Linear issue HAM-18 was used as the source of truth for this implementation.

## Frontend Changes

- Added an admin doctor management route at `/admin/doctors`.
- Protected the route with the existing auth guard and `ADMIN` role metadata.
- Added an admin dashboard entry point for doctor management.
- Added a doctor management page with:
  - Doctor list table.
  - Create doctor form.
  - Edit doctor form.
  - Activate and deactivate actions.
  - Active specialty selector.
  - Field validation and API error handling.
- Added typed API models and services for doctor management and active specialty lookup.
- Extended the shared API service with `PUT` and `PATCH` helpers.

## Backend Contract Used

HAM-18 expects HAM-17 to provide these endpoints:

- `GET /v1/admin/doctors`
- `POST /v1/admin/doctors`
- `PUT /v1/admin/doctors/{id}`
- `PATCH /v1/admin/doctors/{id}/activate`
- `PATCH /v1/admin/doctors/{id}/deactivate`
- `GET /v1/specialties?activeOnly=true`

The doctor UI expects doctor rows with name, email, specialty, status, and consultation duration.

## Tests

Added unit and realistic frontend tests for:

- Admin doctor service endpoint contracts.
- Active specialty lookup endpoint.
- Admin doctor page loading doctors and specialties.
- Create doctor payload normalization.
- Edit doctor without requiring a new password.
- Activate and deactivate workflows.
- Permission error display.
- Admin route protection metadata.

## Verification

- `npm.cmd test -- --watch=false`
  - 9 test files passed.
  - 21 tests passed.
- `npm.cmd run build`
  - Passed.

Full backend-integrated verification of the doctor management workflow depends on HAM-17 implementing the backend API contract above.
