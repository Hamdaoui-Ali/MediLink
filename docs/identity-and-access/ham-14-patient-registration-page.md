# HAM-14 Patient Registration Page

HAM-14 adds the Angular page that lets patients create their own MediLink account from the browser. It connects to the HAM-13 backend endpoint and redirects the patient to login after the account is created.

## What Changed

- Added a typed patient registration model.
- Added `PatientRegistrationService` for calling `POST /v1/patients/register`.
- Added the standalone `/register` page.
- Added client-side validation for required fields, email format, password length, and field length limits.
- Added duplicate-email handling that shows a clear message when the API returns HTTP `409`.
- Updated the app navigation and login page with links to patient registration.
- Added frontend unit tests for the registration service and page behavior.

## User Flow

1. A patient opens `/register`.
2. The patient enters full name, email, password, and phone number.
3. Optional profile fields can be added: date of birth, gender, and address.
4. Angular validates the form before submission.
5. The page sends the request to the backend registration API.
6. On success, the patient is redirected to `/login` with their email in the query string.

The page redirects to login instead of a patient dashboard because registration does not issue a JWT token yet.

## Files

- `medilink-frontend/src/app/features/auth/register.page.*` contains the registration UI and submit behavior.
- `medilink-frontend/src/app/shared/models/patient-registration.model.ts` defines the request and response types.
- `medilink-frontend/src/app/shared/services/patient-registration.service.ts` calls the backend registration endpoint.
- `medilink-frontend/src/app/app.routes.ts` adds the `/register` route.
- `medilink-frontend/src/app/app.html` and `login.page.html` add registration links.
- `medilink-frontend/src/app/features/auth/register.page.spec.ts` tests form behavior and redirects.
- `medilink-frontend/src/app/shared/services/patient-registration.service.spec.ts` tests the API call.

## Verification

Frontend build was run with:

```powershell
npm.cmd run build
```

Frontend tests were run with:

```powershell
npm.cmd test -- --watch=false
```

Result: 3 test files passed, 7 tests passed.

## Out Of Scope

HAM-14 does not automatically authenticate the patient after registration. That requires backend token issuance from the authentication API.
