# HAM-12 Angular Authentication Screens And Token Handling

HAM-12 adds the frontend authentication layer for the Identity and Access milestone. The Angular application now has a login screen, stores the signed-in session locally, attaches the JWT token to API requests, protects role workspaces, and redirects users to the correct dashboard for their role.

The implementation follows the current MediLink direction from the project notes: Angular on the frontend, protected role-specific areas for Admin, Doctor, and Patient users, and JWT-based access for protected API calls. The connected MCP tools did not expose Notion directly in this session, so the frontend contract below is documented explicitly for the backend issue to match.

## What Changed

- Added a standalone login page at `/login`.
- Added an `AuthService` that calls `/auth/login`, normalizes the login response, stores the session in `localStorage`, exposes auth state through Angular signals, and handles logout.
- Added a JWT HTTP interceptor that sends `Authorization: Bearer <token>` on API requests when a user is signed in.
- Replaced the placeholder auth guard with a real guard that blocks logged-out users and enforces role access.
- Added role redirects after login:
  - `ADMIN` goes to `/admin`
  - `DOCTOR` goes to `/doctor`
  - `PATIENT` goes to `/patient`
- Updated the app shell to show Login or Logout depending on session state.

## Expected Backend Contract

The frontend posts login credentials to:

```http
POST /api/v1/auth/login
```

Expected request body:

```json
{
  "email": "admin@medilink.local",
  "password": "password-value"
}
```

The frontend accepts either a direct response body or the existing `ApiResponse<T>` wrapper shape. The payload must include a token, role, and email. This is the preferred response shape:

```json
{
  "data": {
    "accessToken": "jwt-token",
    "tokenType": "Bearer",
    "expiresAt": "2026-06-11T22:00:00Z",
    "user": {
      "id": 1,
      "fullName": "Platform Admin",
      "email": "admin@medilink.local",
      "role": "ADMIN"
    }
  }
}
```

For compatibility while backend auth is still being built, the frontend also accepts `token` or `jwt` as the token field and `role` or the first value of `roles` as the role.

## Files

- `medilink-frontend/src/app/features/auth/login.page.*` contains the login UI.
- `medilink-frontend/src/app/shared/services/auth.service.ts` owns login, logout, session storage, and role redirects.
- `medilink-frontend/src/app/shared/interceptors/jwt-token.interceptor.ts` attaches JWT tokens.
- `medilink-frontend/src/app/shared/guards/auth.guard.ts` protects authenticated and role-specific routes.
- `medilink-frontend/src/app/app.routes.ts` wires `/login`, guards, and role metadata.
- `medilink-frontend/src/app/app.html` and `app.scss` update the visible navigation state.

## Out Of Scope

This issue does not implement backend authentication. Until the backend exposes `POST /auth/login`, the Angular login form can render and submit but cannot complete a real login against the local API.
