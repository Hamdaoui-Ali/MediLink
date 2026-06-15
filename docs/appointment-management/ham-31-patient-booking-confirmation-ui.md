# HAM-31 Appointment Booking Confirmation UI

HAM-31 implements the patient-facing UI for booking an appointment with a doctor and seeing a confirmation after successful booking.

## What Changed

- Created `PatientBookAppointmentPage` standalone Angular component with:
  - A booking form for entering doctor ID, appointment date, start time, and reason for visit.
  - Client-side validation requiring all fields before submission.
  - Loading state with disabled form and "Booking..." button text during submission.
  - Error handling for booking conflicts (409 `SLOT_UNAVAILABLE`), permission errors (403), session expiry (401), and validation errors (400).
  - A confirmation view showing the booked appointment details (doctor ID, date, time range, reason, status) after successful booking.
  - A "Book Another Appointment" button to reset the form and start a new booking.
- Added `BookAppointmentRequest` interface to the shared appointment model.
- Added `bookAppointment(request)` and `listPatientAppointments()` methods to `AppointmentService`.
- Added `/patient/book` route protected by `authGuard` with `PATIENT` role requirement.
- Updated `PatientDashboardPage` with a "Book an Appointment" navigation link matching the doctor dashboard pattern.

## UI Screens

### Booking Form
The form collects four fields:
- **Doctor ID** — numeric input for the doctor's identifier.
- **Appointment Date** — date picker for the desired appointment date.
- **Start Time** — time picker for the desired start time.
- **Reason for Visit** — textarea for briefly describing the visit reason.

The submit button is disabled until all fields are filled. During submission, the button shows "Booking..." and all fields are disabled to prevent double submission. Errors are displayed in a red message box below the form.

### Confirmation Screen
After successful booking, the form is replaced with a green confirmation card showing:
- A "Appointment Confirmed" heading with success message.
- A definition list of appointment details: doctor ID, formatted date, formatted time range, reason, and a `CONFIRMED` status badge.
- A "Book Another Appointment" primary button that resets to the booking form.
- A "Back to Dashboard" link to return to the patient workspace.

### Error States
- **409 Conflict (Slot Unavailable)**: "This time slot is no longer available. Please choose another slot."
- **400 Bad Request**: Shows the server's error message or a fallback.
- **403 Forbidden**: "You do not have permission to perform this action."
- **401 Unauthorized**: "Your session has expired. Please log in again."
- **Network/Fallback**: "Unable to book appointment. Please try again later."

## Architecture

```
medilink-frontend/src/app/
  features/patient/
    patient-book-appointment.page.ts     # Component logic with Angular signals
    patient-book-appointment.page.html   # Template with form + confirmation views
    patient-book-appointment.page.scss   # Styled form, confirmation card, error states
    patient-book-appointment.page.spec.ts# 8 component tests
    patient-dashboard.page.ts            # Updated with navigation link
  shared/
    models/
      appointment.model.ts               # Added BookAppointmentRequest interface
    services/
      appointment.service.ts             # Added bookAppointment(), listPatientAppointments()
  app.routes.ts                          # Added /patient/book route
```

## Routing

```
/patient       → PatientDashboardPage (existing, updated with link)
/patient/book  → PatientBookAppointmentPage (new)
```

## Test Coverage

| Test File | Tests | Type | What it covers |
|-----------|-------|------|---------------|
| `patient-book-appointment.page.spec.ts` | 8 | Component | Component creation, form visibility, disabled button when empty, empty field error, successful booking with HTTP mock, conflict error handling, form reset, confirmation details display |

## Verification

- Frontend: `npm run build` — builds successfully with `patient-book-appointment-page` as a 22.91 kB lazy chunk.
- Backend: `mvnw.cmd test` — 125 tests pass (including 10 new patient appointment integration tests).

## What Is Out Of Scope

- HAM-31 does not include a doctor search page (covered by HAM-27).
- HAM-31 does not include a slot selection page with available time slots (covered by HAM-29).
- HAM-31 does not include the patient's appointment history dashboard (covered by HAM-33).
- The booking form currently accepts a raw doctor ID; a proper doctor search and selection flow will replace this in HAM-27 and HAM-29.
