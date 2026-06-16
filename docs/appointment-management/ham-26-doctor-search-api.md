# HAM-26 Public Doctor Search Backend API

HAM-26 implements the backend API for patients to search and view active doctors.

## What Changed

- Created `PatientDoctorController` at `/v1/patient/doctors` (PATIENT role) with:
  - `GET /v1/patient/doctors` — search/list active doctors with optional `specialtyId` and `name` filters.
  - `GET /v1/patient/doctors/{id}` — view doctor profile details.
- Created `DoctorSearchService` with `searchDoctors(specialtyId, name)` and `getDoctor(id)` methods.
- Added query methods to `DoctorRepository`:
  - `findAllActiveWithDetails()` — all active doctors with JOIN FETCH on specialty and user.
  - `findByIdAndStatusActive(Long id)` — single active doctor by ID.
  - `findBySpecialtyIdAndStatusActive(Long specialtyId)` — filter by specialty.
  - `findByUserFullNameContainingIgnoreCaseAndStatusActive(String name)` — search by name.
- Created `DoctorSummaryResponse` DTO (patient-safe fields: id, fullName, specialtyName, biography, consultationDurationMinutes, clinicAddress).
- Created `DoctorSearchMapper` component for entity-to-DTO mapping.
- Inactive and disabled doctors are excluded from all search results.

## Test Coverage

| Test | Tests | Type |
|------|-------|------|
| `PatientDoctorSearchIntegrationTest` | 6 | Integration |
