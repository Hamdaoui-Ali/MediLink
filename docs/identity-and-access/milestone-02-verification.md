# Milestone 02 Identity And Access Verification

This note records the verification pass after completing the remaining Identity and Access issues.

## Issues Covered

- HAM-10: Backend user, role, and account status model.
- HAM-11: Authentication APIs with JWT.
- HAM-12: Angular authentication screens and token handling.
- HAM-13: Patient registration backend API.
- HAM-14: Patient registration page.

HAM-15 was implemented before the instruction to stay within the Identity and Access milestone. HAM-16 was moved back to Backlog and no HAM-16 file edits were made.

## Backend Verification

Command:

```powershell
.\mvnw.cmd test
```

Result:

- 43 tests passed.
- 0 failures.
- 0 errors.

Covered backend behavior includes:

- User and role model behavior.
- Email uniqueness handling.
- Account status changes.
- BCrypt password hashing.
- Patient registration and duplicate email handling.
- Login with valid credentials.
- Safe invalid credential errors.
- Inactive account rejection.
- JWT access token generation.
- JWT role mapping for protected endpoints.
- Current-user response behavior.

## Frontend Verification

Command:

```powershell
npm.cmd test -- --watch=false
```

Result:

- 4 test files passed.
- 10 tests passed.

Covered frontend behavior includes:

- App shell rendering.
- Patient registration service API calls.
- Patient registration form validation, request normalization, duplicate-email errors, and login redirect.
- Auth service login endpoint, session storage, role redirect, and logout.

## Frontend Build

Command:

```powershell
npm.cmd run build
```

Result: production build completed successfully.

## Current Status

The Identity and Access milestone is ready for review. The issues are not marked Done automatically because the project rules require user approval before moving issues to Done.
