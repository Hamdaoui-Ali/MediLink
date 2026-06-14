# HAM-04 Doctor Profile Backend APIs

HAM-04 implements the backend API that allows doctors to view and update their own profile information. Admin-only fields such as status and specialty remain protected from self-update.

## What Changed

- Created `Doctor` JPA entity with `@ManyToOne` relationships to `User` and `Specialty`, mapping to the `doctors` table.
- Created `DoctorRepository` with `findByUserId(Long userId)` query method.
- Created `DoctorProfileService` with:
  - `getProfile(userId)` — returns doctor profile with user details and specialty name.
  - `updateProfile(userId, request)` — updates only allowed fields: biography, clinic address, consultation duration, and phone number.
- Created `DoctorProfileController` at `/v1/doctor/profile` with GET and PATCH endpoints, protected with `@PreAuthorize("hasRole('DOCTOR')")`.
- Created `DoctorProfileResponse` DTO with 11 fields including specialty name, account status, and contact info.
- Created `DoctorProfileUpdateRequest` DTO with only 4 doctor-editable fields.
- Added `updatePhoneNumber(String)` method to `User` entity.
- Added `DoctorNotFoundException` with handler in `GlobalExceptionHandler`.

## API Endpoints

| Method | Path | Access | Purpose |
|--------|------|--------|---------|
| GET | `/v1/doctor/profile` | DOCTOR | View own profile |
| PATCH | `/v1/doctor/profile` | DOCTOR | Update allowed profile fields |

### GET Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "userId": 5,
    "fullName": "Dr. Test",
    "email": "dr.test@medilink.local",
    "phoneNumber": "+15551234567",
    "accountStatus": "ACTIVE",
    "specialtyName": "Cardiology",
    "biography": "Experienced cardiologist",
    "consultationDurationMinutes": 30,
    "clinicAddress": "123 Heart Lane",
    "status": "ACTIVE"
  }
}
```

### PATCH Request (all fields optional)

```json
{
  "biography": "Updated bio",
  "clinicAddress": "456 New Clinic",
  "consultationDurationMinutes": 45,
  "phoneNumber": "+15559999999"
}
```

## Field Access Rules

| Field | Doctor Can View | Doctor Can Edit | Admin Only |
|-------|:---:|:---:|:---:|
| fullName | ✅ | ❌ | ❌ |
| email | ✅ | ❌ | ❌ |
| phoneNumber | ✅ | ✅ | ❌ |
| accountStatus | ✅ | ❌ | ✅ |
| specialtyName | ✅ | ❌ | ✅ |
| biography | ✅ | ✅ | ❌ |
| clinicAddress | ✅ | ✅ | ❌ |
| consultationDurationMinutes | ✅ | ✅ | ❌ |
| status (ACTIVE/INACTIVE) | ✅ | ❌ | ✅ |

## Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/doctor/
  domain/
    Doctor.java                     # JPA entity with @ManyToOne to User and Specialty
  repository/
    DoctorRepository.java           # JPA repo: findByUserId
  service/
    DoctorProfileService.java       # Business logic + field access control
    DoctorNotFoundException.java    # Custom exception
  web/
    DoctorProfileController.java    # REST controller at /v1/doctor/profile
    DoctorProfileResponse.java      # Response DTO (11 fields)
    DoctorProfileUpdateRequest.java # Request DTO (4 editable fields)
```

## Access Control

- Both endpoints require the `DOCTOR` role via `@PreAuthorize("hasRole('DOCTOR')")`.
- Doctor identity is resolved directly from the JWT `userId` claim.
- A doctor can only access their own profile (via `findByUserId(userId)`).
- Unauthenticated requests receive 401. Non-doctor requests (e.g., patient) receive 403.

## Test Coverage

| Test File | Tests | Type | What it covers |
|-----------|-------|------|---------------|
| `DoctorProfileServiceTest` | 5 | Unit | Get profile, get not found, update fields, preserve existing on null, admin fields unchanged |
| `DoctorProfileControllerTest` | 3 | Unit | GET returns profile, PATCH updates, @PreAuthorize check |
| `DoctorProfileIntegrationTest` | 5 | Integration | View profile (full HTTP + DB), update profile + verify phone persisted, status/specialty not editable, unauthenticated 401, patient 403 |

## Verification

- Backend: `mvnw.cmd test` — 125 tests passed, 0 failures, 0 errors

## What Is Out Of Scope

- HAM-04 does not build the frontend profile page (separate issue).
- HAM-04 does not implement admin profile management (only doctor self-service).
- HAM-04 does not implement profile picture or document upload.
- HAM-04 does not allow doctors to change their email or full name (these are identity fields managed by admin).
