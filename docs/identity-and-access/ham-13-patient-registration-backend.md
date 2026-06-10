# HAM-13 Patient Registration Backend API

HAM-13 adds the backend endpoint that lets patients create their own MediLink accounts. The endpoint creates both the shared login account and the linked patient profile in one transaction, so the database does not end up with a user account that is missing its patient record.

## What Changed

- Added JPA entities for the existing `roles`, `users`, and `patients` tables.
- Added repositories for roles, users, and patients.
- Added a patient registration service that:
  - normalizes email addresses before duplicate checks,
  - rejects duplicate email registration,
  - loads the seeded `PATIENT` role,
  - hashes the password with BCrypt,
  - creates an active user account,
  - creates the linked patient profile.
- Added `POST /api/v1/patients/register` as the public registration endpoint.
- Added duplicate-email handling that returns HTTP `409 Conflict`.
- Updated security configuration so patient registration is allowed without an existing login.
- Fixed the Windows Maven wrapper script so backend tests can run when the local `.m2` directory is not a symbolic link.

## API Contract

Request:

```http
POST /api/v1/patients/register
Content-Type: application/json
```

```json
{
  "fullName": "Jane Patient",
  "email": "jane@example.com",
  "password": "Patient@123",
  "phoneNumber": "+15551234567",
  "dateOfBirth": "1990-03-05",
  "gender": "FEMALE",
  "address": "100 Care Street"
}
```

Required fields are `fullName`, `email`, `password`, and `phoneNumber`. `dateOfBirth`, `gender`, and `address` are optional patient profile fields.

Successful response:

```json
{
  "success": true,
  "data": {
    "userId": 1,
    "patientId": 1,
    "fullName": "Jane Patient",
    "email": "jane@example.com",
    "phoneNumber": "+15551234567",
    "role": "PATIENT",
    "accountStatus": "ACTIVE"
  },
  "error": null,
  "timestamp": "2026-06-10T22:40:00Z"
}
```

Duplicate email response:

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "DUPLICATE_EMAIL",
    "message": "An account with this email already exists",
    "details": {}
  },
  "timestamp": "2026-06-10T22:40:00Z"
}
```

## Files

- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/domain` contains the shared user and role model.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/repository` contains identity repositories.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/patient/domain` contains the patient profile model.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/patient/service/PatientRegistrationService.java` owns registration behavior.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/patient/web/PatientRegistrationController.java` exposes the endpoint.
- `medilink-backend/src/test/java/com/medilink/medilink_backend/patient` contains focused unit tests for the new registration behavior.

## Verification

Backend tests were run with:

```powershell
.\mvnw.cmd test
```

Result: 10 tests passed, 0 failures, 0 errors.

## Out Of Scope

HAM-13 does not log the patient in after registration and does not issue JWT tokens. Login and token behavior remain part of the authentication API work.
