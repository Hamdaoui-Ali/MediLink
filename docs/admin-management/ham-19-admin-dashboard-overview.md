# HAM-19 Admin Dashboard Overview

HAM-19 builds the admin dashboard overview, giving administrators a single-page snapshot of the platform after login. The dashboard displays key metrics, quick links to management pages, and a recent appointments list.

## What Changed

- Created dashboard-specific read-only JPA entities (`DoctorDashboard`, `AppointmentDashboard`) that map to the existing database tables without creating entities that would conflict with future full-featured implementations.
- Built `AdminDashboardService` that aggregates platform statistics from existing repositories (patients, specialties) and the new dashboard repositories (doctors, appointments).
- Added `AdminDashboardController` at `GET /v1/admin/dashboard` with admin-only access via `@PreAuthorize("hasRole('ADMIN')")`.
- Created `DashboardOverviewResponse` DTO containing total counts for doctors, patients, appointments, and specialties, plus a list of the five most recent appointments with doctor and patient names.
- Added `DashboardOverview` TypeScript model and `AdminDashboardService` frontend API service that calls the backend dashboard endpoint.
- Redesigned `AdminDashboardPage` with an external template and stylesheet, replacing the previous inline template.
- Built a dashboard UI with four stat cards (Active Doctors, Total Patients, Total Appointments, Active Specialties), management quick links (Specialties, Doctors), and a recent appointments table with status pill badges.
- Added loading, error, and empty states for the dashboard component.

## Backend Architecture

The dashboard backend lives in its own subpackage to stay self-contained and avoid conflicts with future entities:

```
medilink-backend/src/main/java/com/medilink/medilink_backend/administration/dashboard/
  domain/
    DoctorDashboard.java              # Read-only entity for doctors table
    AppointmentDashboard.java         # Read-only entity for appointments table
  repository/
    DoctorDashboardRepository.java    # JPA repository with countByStatus
    AppointmentDashboardRepository.java # JPA repository with findTop5ByOrderByCreatedAtDesc
  service/
    AdminDashboardService.java        # Aggregates stats from four repositories
  web/
    AdminDashboardController.java     # GET /v1/admin/dashboard
    DashboardOverviewResponse.java    # Response DTO with counts + recent appointments
```

The `DoctorDashboard` and `AppointmentDashboard` entities are intentionally minimal read-only entities. They use `insertable = false, updatable = false` on all column mappings and provide only getters. This prevents them from being used for writes and makes it clear they are data-access only for the dashboard. Future issues (HAM-17 for doctor management, HAM-30+ for appointments) will create proper writable entities in their own domain packages.

The service uses JPA `findAllById` to resolve doctor and patient names for recent appointments in two queries instead of N+1 queries per appointment.

## Frontend Architecture

```
medilink-frontend/src/app/
  shared/
    models/
      dashboard-overview.model.ts     # DashboardOverview and RecentAppointment interfaces
    services/
      admin-dashboard.service.ts      # API service wrapping GET /v1/admin/dashboard
      admin-dashboard.service.spec.ts # Service unit tests (HTTP contract verification)
  features/admin/
    admin-dashboard.page.ts           # Enhanced dashboard component with signals
    admin-dashboard.page.html         # Dashboard template with control flow
    admin-dashboard.page.scss         # Dashboard styles
    admin-dashboard.page.spec.ts      # Component tests (8 tests)
```

The dashboard component uses Angular signals for reactive state management:
- `overview` signal holds the dashboard data or null
- `loading` signal controls the loading spinner
- `error` signal shows error messages with a retry button

## UI Features

- **Stat cards**: Four cards displaying Active Doctors, Total Patients, Total Appointments, and Active Specialties counts. Each card shows a large blue number with a descriptive label.
- **Management quick links**: Two navigation cards linking to `/admin/specialties` and `/admin/doctors` management pages.
- **Recent appointments table**: Shows the five most recent appointments with columns for date, time, doctor name, patient name, and status (with color-coded pill badges).
- **Loading state**: Centered message "Loading dashboard data..." shown while the API request is in progress.
- **Error state**: Red error banner with a "Try again" button that re-fetches dashboard data on click.
- **Empty state**: "No appointments yet." message shown when the recent appointments list is empty.
- **Status pills**: Color-coded status badges: blue (confirmed), red (cancelled), green (completed), amber (missed), indigo (rescheduled).

## Access Rules

- Only authenticated users with the `ADMIN` role can access the dashboard endpoint.
- The backend controller enforces admin-only access via `@PreAuthorize("hasRole('ADMIN')")`.
- The frontend route `/admin` is already protected by `authGuard` with `roles: ['ADMIN']` from the HAM-12 authentication setup.
- Unauthenticated requests to the dashboard API receive 401 UNAUTHORIZED.
- Non-admin authenticated requests (patient, doctor) receive 403 FORBIDDEN.

## Test Coverage

### Backend

| Test | Type | What it covers |
|------|------|---------------|
| `AdminDashboardServiceTest` | Unit (3 tests) | Service methods return correct counts, recent appointments mapping, empty dashboard handling |
| `AdminDashboardControllerTest` | Unit (3 tests) | Controller wraps data in ApiResponse, includes recent appointments, requires admin role |
| `AdminDashboardIntegrationTest` | Integration (5 tests) | Full HTTP lifecycle: admin access, patient 403, anonymous 401, data consistency across requests, recent appointments presence |

### Frontend

| Test | Type | What it covers |
|------|------|---------------|
| `admin-dashboard.service.spec.ts` | Unit (2 tests) | HTTP GET contract, empty data handling |
| `admin-dashboard.page.spec.ts` | Component (8 tests) | Loading on init, stat card values, management links, recent appointments table, loading state, error state, empty state, retry on error |

## Verification

- Backend: `mvnw.cmd test` — 64 tests passed, 0 failures, 0 errors
- Frontend tests: `npm.cmd test -- --watch=false` — 10 test files passed, 32 tests passed
- Frontend build: `npm.cmd run build` — production build completed successfully

## What Is Out Of Scope

- HAM-19 does not implement the doctor management page linked from the dashboard. That is covered by HAM-18 (unmerged branch `feature/ham-18-admin-doctor-management-ui`).
- HAM-19 does not create full Doctor or Appointment domain entities. Dashboard entities are read-only mappings for statistics only.
- HAM-19 does not add appointment listing, filtering, or detail views. Those belong to appointment management issues (HAM-30+).
- HAM-19 does not implement real-time updates or websocket-based dashboard refreshes. The dashboard is a static snapshot fetched on page load and on manual retry.
