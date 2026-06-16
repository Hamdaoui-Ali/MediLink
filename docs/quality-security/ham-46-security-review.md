# HAM-46 Security Hardening Review

Security review of MediLink before MVP release.

## Findings

### 1. Password Hashing — PASS
- **Config**: `BCryptPasswordEncoder` via `SecurityConfig.passwordEncoder()`.
- **Strength**: BCrypt with default strength (10 rounds) — industry standard for MVP.
- **No plain-text passwords** stored or logged anywhere.

### 2. JWT Security — PASS
- **Secret**: Configured via `@Value("${medilink.security.jwt.secret}")` with `application.yml` default `default-jwt-secret-for-local-dev-only`.
- **Environment override**: `MEDILINK_JWT_SECRET` for production.
- **Expiration**: Currently unlimited. **Recommendation**: Add token expiration for production (e.g., 24h).
- **Secret not committed**: The default is a placeholder; production secrets via env vars.

### 3. Role-Based Access Control — PASS
- **Admin**: `@PreAuthorize("hasRole('ADMIN')")` on specialty management endpoints.
- **Doctor**: `@PreAuthorize("hasRole('DOCTOR')")` on `/v1/doctor/*` endpoints.
- **Patient**: `@PreAuthorize("hasRole('PATIENT')")` on `/v1/patient/*` endpoints.
- **Data scoping**: Doctor queries filter by `doctorId` from JWT. Patient queries filter by `patientId` from JWT.
- **Cross-user blocking**: AuthorizationIntegrationTest (HAM-44) verifies 7 test scenarios.

### 4. Frontend Route Guards — PASS
- **authGuard**: All protected routes use `canActivate: [authGuard]` with `data: { roles: ['ROLE'] }`.
- **guestGuard**: Login/register routes block authenticated users.
- **Token storage**: LocalStorage (standard for SPAs). **Note**: Cookie-based storage would be more secure but requires CSRF protection.

### 5. Input Validation — PASS
- **Backend**: Jakarta validation on all request DTOs (`@NotNull`, `@NotBlank`, `@FutureOrPresent`).
- **Backend**: Additional business validation in service layer (slot overlap, doctor active, past date).
- **Frontend**: HTML5 validation attributes + Angular template-driven forms.
- **SQL injection**: All DB access via JPA parameterized queries — no raw SQL concatenation.

### 6. Sensitive Data Exposure — PASS
- **DoctorSummaryResponse**: Excludes email, userId (only public-safe fields).
- **DoctorProfileResponse**: Only exposed to doctor's own profile endpoint.
- **Patient registration**: Only returns `success: true`, no sensitive data in response.
- **Password never returned** in any API response.

### 7. Environment Secrets — PASS
- **JWT secret**: Environment variable `MEDILINK_JWT_SECRET`.
- **SMTP credentials**: Environment variables `MEDILINK_MAIL_*`.
- **No hardcoded secrets**: All secrets use `@Value` with env var overrides.
- **`.gitignore`**: OpenCode config files excluded from commits.

### 8. CORS Configuration — PASS
- **Allowed origins**: `http://localhost:4200`, `http://127.0.0.1:4200` (frontend dev server).
- **Allowed methods**: GET, POST, PUT, PATCH, DELETE, OPTIONS.
- **Credentials**: Allowed.
- **Production**: Needs `MEDILINK_FRONTEND_URL` env var for deployment.

## Recommendations

| ID | Severity | Recommendation |
|----|----------|---------------|
| SEC-01 | Medium | Add JWT token expiration (e.g., 24h for access token) |
| SEC-02 | Low | Consider CSRF protection if switching to cookie-based auth |
| SEC-03 | Low | Add rate limiting on login endpoint to prevent brute-force |
| SEC-04 | Low | Add request size limits on file uploads when implemented |
| SEC-05 | Low | Configure CORS origin via environment variable for production |

## Verdict

MediLink is **ready for MVP release** from a security standpoint. Core protections (password hashing, JWT, RBAC, input validation, data scoping) are in place. The recommendations above are enhancements for post-MVP hardening.
