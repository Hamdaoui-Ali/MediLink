# HAM-10 User, Role, And Account Status Model

HAM-10 adds the reusable backend identity model used by admin, doctor, and patient accounts. The database schema already defined the identity tables, and this issue adds the Java model, repositories, and service layer that later features can use safely.

## What Changed

- Added `User` and `Role` JPA entities for the existing `users` and `roles` tables.
- Added `AccountStatus` values:
  - `ACTIVE`
  - `INACTIVE`
  - `DISABLED`
- Added `RoleName` values:
  - `ADMIN`
  - `DOCTOR`
  - `PATIENT`
- Added repositories for role and user lookup.
- Added `UserAccountService` for reusable user creation, email lookup, and account status changes.
- Added duplicate-email, missing-role, and missing-user exceptions with API error mappings.
- Added focused backend unit tests for user creation, email uniqueness, role lookup, account status changes, and identity error handling.

## Why It Matters

MediLink users share the same login and account data, but each role has different profile details. This identity model keeps the shared account fields in one place while allowing patient and doctor features to attach their own profile records.

## Developer Notes

The reusable service creates users from `CreateUserCommand`. It expects the password field to already be hashed. HAM-11 owns login and password hashing for authentication, while HAM-13 uses hashing during patient registration.

Email addresses are normalized before lookup and creation. The database also enforces uniqueness through `uq_users_email`.

## Files

- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/domain` contains identity entities and enums.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/repository` contains role and user repositories.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/service` contains reusable user account behavior.
- `medilink-backend/src/test/java/com/medilink/medilink_backend/identity/service/UserAccountServiceTest.java` verifies the identity service.

## Verification

Backend tests were run with:

```powershell
.\mvnw.cmd test
```

Result: 32 tests passed, 0 failures, 0 errors.
