# HAM-21 Doctor Profile UI

HAM-21 builds the doctor profile page where doctors can view their account information and update their editable profile fields. Admin-only fields like specialty, email, and status are displayed but not editable.

## What Changed

- Created `DoctorProfile` and `DoctorProfileUpdateRequest` models in `shared/models/doctor-profile.model.ts`.
- Created `DoctorProfileService` in `shared/services/doctor-profile.service.ts` with `getProfile()` and `updateProfile()` methods calling `/v1/doctor/profile`.
- Created `DoctorProfilePage` component with a two-column layout: account info (read-only) on the left, edit form on the right.
- Editable fields: biography, clinic address, phone number, consultation duration.
- Read-only fields: full name, email, specialty, account status, doctor status (shown with badges).
- Validation: biography max 2000 chars, clinic address max 500 chars, consultation duration min 5 minutes.
- Admin notice: "Your full name, email, specialty, and status can only be changed by an administrator."
- Loading, success, and error states.
- Added `/doctor/profile` route protected by authGuard with DOCTOR role.
- Updated doctor dashboard with "My Profile" navigation link.

## UX Flow

1. Doctor navigates to `/doctor/profile` (or clicks "My Profile" from dashboard).
2. Account info section shows name, email, specialty, and status badges.
3. Edit form shows current values for biography, clinic address, phone, and consultation duration.
4. Doctor modifies fields and clicks "Save Changes".
5. Success/error message displayed; profile data refreshed on success.

## Edit vs Read-only Fields

| Field | Viewable | Editable |
|-------|:---:|:---:|
| Full Name | ✅ | ❌ |
| Email | ✅ | ❌ |
| Phone Number | ✅ | ✅ |
| Specialty | ✅ | ❌ |
| Account Status | ✅ | ❌ |
| Doctor Status | ✅ | ❌ |
| Biography | ✅ | ✅ |
| Clinic Address | ✅ | ✅ |
| Consultation Duration | ✅ | ✅ |

## APIs Used

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/v1/doctor/profile` | Load profile data |
| PATCH | `/v1/doctor/profile` | Update editable fields |

## Test Coverage (9 new tests)

| Test File | Tests | What it covers |
|-----------|-------|---------------|
| `doctor-profile.service.spec.ts` | 2 | GET profile, PATCH update |
| `doctor-profile.page.spec.ts` | 7 | Init load, form population, permission error (403), save profile, invalid form blocked, save error (401), whitespace trimming |

## Verification

- Frontend build: succeeds
- Frontend tests: 61 passed, 0 failures

## What Is Out Of Scope

- HAM-21 does not implement admin profile management (separate issue).
- HAM-21 does not implement profile picture or document upload.
- HAM-21 does not allow doctors to change their email or name.
