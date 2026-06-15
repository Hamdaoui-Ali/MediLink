# HAM-27 Patient Doctor Search UI

HAM-27 implements the patient-facing page for searching doctors and viewing their profiles.

## What Changed

- Created `PatientDoctorSearchPage` at `/patient/doctors` with:
  - Search bar for filtering by doctor name (enter key or button).
  - Doctor cards displaying: full name, specialty badge, biography, consultation duration, and clinic address.
  - "View Available Slots" button on each card linking to `/patient/slots/:doctorId`.
  - Loading state during search.
  - Empty state when no doctors found.
  - Error handling for 401/403.
- Added `DoctorSummary` interface to the doctor profile model.
- Added `searchDoctors(specialtyId?, name?)` method to `DoctorProfileService`.
- Added `/patient/doctors` route protected by `authGuard` with `PATIENT` role.
- Updated `PatientDashboardPage` with "Find a Doctor" navigation link.
