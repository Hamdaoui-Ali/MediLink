# HAM-16 Admin Specialty Management UI

HAM-16 builds the Angular admin interface for managing medical specialties, using the backend APIs created in HAM-15. This gives administrators a full CRUD UI for listing, creating, editing, activating, and deactivating clinical specialties.

## What Changed

- Added `Specialty` and `SpecialtyRequest` frontend models matching the backend HAM-15 API contract.
- Added `SpecialtyManagementService` as a shared API service wrapping the `/api/v1/specialties` endpoints with list-all, create, update, activate, and deactivate methods.
- Built `AdminSpecialtyManagementPage` as a two-panel admin page:
  - Left panel: a sortable table of all specialties (active and inactive) with per-row edit, activate, and deactivate actions.
  - Right panel: a form that switches between create and edit modes, with field validation and error/success feedback.
- Registered the route `/admin/specialties` with admin-only role protection via `authGuard`.
- Linked the route from the admin dashboard navigation card.
- Extended `ApiService` with `put` and `patch` HTTP methods needed by the specialty management service.
- Added frontend unit tests for the specialty management service (HTTP contract verification), the admin page component (create, edit, activate, deactivate flows, error handling), and the route guard configuration.

## UI Features

- **List panel**: Displays all specialties in a table with name, status (pill badge), description, and action buttons. Selecting a row highlights it and loads it into the edit form.
- **Create/Edit form**: Shared form panel that handles both new and existing specialties. Shows the current status when editing.
- **Status toggle**: Active specialties show a "Deactivate" button; inactive ones show "Activate". Status changes are applied immediately via the backend PATCH endpoints without a full page reload.
- **Loading states**: The list shows a loading message during data fetch, and the submit button disables during save operations with a "Saving..." label.
- **Error handling**: Specific messages for 403 (permission denied), 409 (duplicate name), and generic fallback errors.
- **Success feedback**: Green success banner after create, update, activate, or deactivate operations.
- **Refresh**: Manual refresh button to reload the specialty list.

## Access Rules

- Only authenticated users with the `ADMIN` role can access `/admin/specialties`.
- The route guard (`authGuard`) redirects non-admin users to their role's default dashboard.
- Unauthenticated users are redirected to `/login`.

## Files

- `medilink-frontend/src/app/shared/models/specialty.model.ts` — `Specialty`, `SpecialtyStatus`, `SpecialtyRequest` type definitions.
- `medilink-frontend/src/app/shared/services/specialty-management.service.ts` — API service for specialty CRUD operations.
- `medilink-frontend/src/app/shared/services/specialty-management.service.spec.ts` — HTTP contract verification tests.
- `medilink-frontend/src/app/features/admin/admin-specialty-management.page.ts` — Main page component with create, edit, activate, and deactivate logic.
- `medilink-frontend/src/app/features/admin/admin-specialty-management.page.html` — Two-panel template (list + form).
- `medilink-frontend/src/app/features/admin/admin-specialty-management.page.scss` — Responsive layout and styling.
- `medilink-frontend/src/app/features/admin/admin-specialty-management.page.spec.ts` — Component tests for CRUD flows, status changes, and error handling.
- `medilink-frontend/src/app/features/admin/admin-dashboard.page.ts` — Updated with navigation link to specialties management.
- `medilink-frontend/src/app/app.routes.ts` — Added `/admin/specialties` route with admin role guard.
- `medilink-frontend/src/app/app.routes.spec.ts` — Route configuration test verifying admin protection.
- `medilink-frontend/src/app/shared/services/api.service.ts` — Extended with `put` and `patch` methods.

## Verification

Frontend tests:

```powershell
cd medilink-frontend
npm.cmd test -- --watch=false
```

Result: 22 tests passed, 0 failures, 0 errors across 8 test files.

HAM-16-specific test coverage includes:

- Unit tests for `SpecialtyManagementService` verifying HTTP method, URL, and request body for list-all (GET), create (POST), update (PUT), activate (PATCH), and deactivate (PATCH) endpoints against the `ApiResponse` envelope.
- Component tests for `AdminSpecialtyManagementPage` covering: specialty list loading on init, form value normalization into request objects, specialty creation and list insertion, editing an existing specialty without creating a duplicate, activate/deactivate status toggle operations, and permission error message display on 403 responses.
- Route guard test verifying `/admin/specialties` requires `authGuard` and restricts access to the `ADMIN` role.

Frontend production build:

```powershell
cd medilink-frontend
npm.cmd run build
```

Result: Build completed successfully with all lazy chunks (including `admin-specialty-management-page`) generated.

## Out Of Scope

HAM-16 covers only the specialty management UI. The following are handled by later issues:

- HAM-17: Doctor account management backend.
- HAM-18: Admin doctor management UI.
- Any additional admin management pages beyond specialties (doctors, patients, appointments) are not included.
