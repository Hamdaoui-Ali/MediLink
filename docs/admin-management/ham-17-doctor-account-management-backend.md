# HAM-17 - Doctor Account Management Backend APIs

## Scope

Implemented backend APIs that allow admins to create and manage doctor accounts and linked doctor profiles.

Notion context was requested by the project rules, but the Notion connector is not available in this Codex session. Linear issue HAM-17 was used as the source of truth.

## Backend Changes

- Added doctor domain model and status enum backed by the existing `doctors` table.
- Added doctor repository queries that fetch linked user and specialty data for API responses.
- Added doctor management service for:
  - Listing doctors.
  - Creating a DOCTOR user with an encoded password.
  - Creating the linked doctor profile.
  - Updating doctor user/profile data.
  - Optional password update during edit.
  - Activating and deactivating doctor accounts.
- Kept doctor profile status and user account status synchronized.
- Added admin-only REST controller at `/v1/admin/doctors`.
- Added API error handling for missing doctors and invalid doctor create requests.
- Added user profile/password update helpers required by doctor management.

## API Contract

- `GET /v1/admin/doctors`
- `POST /v1/admin/doctors`
- `PUT /v1/admin/doctors/{id}`
- `PATCH /v1/admin/doctors/{id}/activate`
- `PATCH /v1/admin/doctors/{id}/deactivate`

All endpoints require the `ADMIN` role.

## Tests

Added unit tests for:

- Doctor domain update and status synchronization behavior.
- User profile and password hash update helpers.
- Doctor management service create/list/update/activate/deactivate behavior.
- Admin doctor controller responses and role protection metadata.

Added real integration tests for:

- Admin creates a doctor through HTTP.
- Created doctor can log in with the configured credentials.
- Admin updates doctor details through HTTP.
- Admin lists doctors and sees the created account.
- Admin deactivates a doctor and login is blocked.
- Admin reactivates a doctor and login works again.
- Anonymous and non-admin users cannot create doctors.
- Missing password and duplicate email failures return API errors.

## Verification

- `.\mvnw.cmd test`
  - 71 tests passed.
  - 0 failures.
  - 0 errors.
