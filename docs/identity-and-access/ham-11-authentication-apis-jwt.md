# HAM-11 Authentication APIs With JWT

HAM-11 adds login and token-based authentication for MediLink. Users can authenticate with email and password, receive a JWT access token, and call a current-user endpoint to identify the authenticated account and role.

## What Changed

- Added `POST /api/v1/auth/login`.
- Added `GET /api/v1/auth/me`.
- Added password verification using the shared BCrypt `PasswordEncoder`.
- Added JWT access-token generation with role and identity claims.
- Added JWT decoder configuration for protected backend requests.
- Added JWT role mapping so claims like `"role": "ADMIN"` become Spring authorities like `ROLE_ADMIN`.
- Added safe errors for invalid credentials and inactive accounts.
- Updated frontend auth to call `/v1/auth/login`, matching the backend endpoint.
- Added backend and frontend unit tests for authentication behavior.

## Login Contract

Request:

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@medilink.local",
  "password": "Admin@12345"
}
```

Successful response:

```json
{
  "success": true,
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresAt": "2026-06-11T22:00:00Z",
    "user": {
      "id": 1,
      "fullName": "Local Admin",
      "email": "admin@medilink.local",
      "role": "ADMIN",
      "accountStatus": "ACTIVE"
    }
  },
  "error": null,
  "timestamp": "2026-06-10T22:55:00Z"
}
```

Invalid credentials return `401 Unauthorized` with `INVALID_CREDENTIALS`. Inactive or disabled accounts return `403 Forbidden` with `INACTIVE_ACCOUNT`.

## Current User Contract

```http
GET /api/v1/auth/me
Authorization: Bearer <jwt-token>
```

The endpoint returns the current authenticated user with their role and account status.

## Configuration

JWT signing uses:

```yaml
medilink.security.jwt.secret
```

For local development, the application has a fallback secret. Production deployments must set `MEDILINK_JWT_SECRET`.

## Files

- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/service/AuthenticationService.java` verifies credentials and builds login responses.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/service/JwtTokenService.java` creates access tokens.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/web/AuthenticationController.java` exposes login and current-user endpoints.
- `medilink-backend/src/main/java/com/medilink/medilink_backend/shared/config/SecurityConfig.java` configures JWT security.
- `medilink-frontend/src/app/shared/services/auth.service.ts` calls the backend login endpoint.

## Verification

Backend tests were run with:

```powershell
.\mvnw.cmd test
```

Result: 43 tests passed, 0 failures, 0 errors.

Frontend milestone tests were also updated to cover the auth service endpoint and session behavior.
