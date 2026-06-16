# HAM-39 Doctor Appointment Notes and Status UI

HAM-39 adds an appointment detail panel to the doctor appointments page so doctors can view details, add private clinical notes, and update appointment status directly from the dashboard.

## What Changed

### Frontend

- Added `STATUS_TRANSITIONS` map and `isTerminalStatus()` helper to `appointment.model.ts`.
- Added `getAppointment()`, `updateNotes()`, and `updateStatus()` methods to `AppointmentService`.
- Upgraded `DoctorAppointmentsPage` with a split layout: appointment list on the left, detail panel on the right.
- Detail panel shows: patient name, date, time, reason, current status badge.
- Status update controls: color-coded action buttons showing only valid transitions for the current status (e.g., CONFIRMED shows Complete, Cancel, Miss, Reschedule buttons).
- Private notes: textarea pre-populated with existing notes, with Save Notes button.
- Terminal states (COMPLETED, CANCELLED, MISSED) show a notice and hide status change buttons.
- Loading/saving states for notes and status updates with success/error messages.
- Selecting an appointment highlights it in the list and opens the detail panel.

## UX Flow

1. Doctor navigates to `/doctor/appointments`.
2. Clicks an appointment row in the list (left side).
3. Detail panel opens (right side) showing full appointment info.
4. Doctor can:
   - Edit private clinical notes in the textarea and click "Save Notes".
   - Update status by clicking one of the colored action buttons (only valid transitions shown).
   - Close the detail panel with the "Close" button.
5. Updates are reflected in the list immediately.

## Status Transition Rules

| Current Status | Valid Transitions |
|---------------|-------------------|
| CONFIRMED | COMPLETED, CANCELLED, MISSED, RESCHEDULED |
| RESCHEDULED | CONFIRMED, COMPLETED, CANCELLED, MISSED |
| COMPLETED | (none - terminal) |
| CANCELLED | (none - terminal) |
| MISSED | (none - terminal) |

Invalid transitions (e.g., trying to change a completed appointment) are prevented in the UI (buttons hidden) and rejected by the backend with a 400 error.

## Private Notes

- Notes are visible only to doctors (enforced by the backend — the patient appointment endpoint, when built, will exclude `doctorNotes` from the response).
- Notes are saved via `PATCH /v1/doctor/appointments/{id}/notes`.
- The textarea is pre-populated with any existing notes when an appointment is selected.

## Test Coverage (13 component tests)

| Test | What it covers |
|------|---------------|
| Init load | Appointments load on init |
| Permission error | 403 shows permission message |
| Select appointment | Select populates notes draft and clears messages |
| Clear selection | Close button clears selected appointment |
| Save notes | Updates notes in list and shows success message |
| Notes error | 400 error shown on notes save failure |
| Update status | Marks completed, updates list, shows success |
| Valid transitions | CONFIRMED returns 4 valid transitions |
| Terminal transitions | COMPLETED returns empty transitions, isTerminal true |
| No selection terminal | isTerminal false when nothing selected |
| Time formatting | 12h AM/PM format |
| Date formatting | Locale-formatted dates |
| Status labels | All 5 statuses return correct label |

## APIs Used

| Method | Path | Purpose |
|--------|------|---------|
| GET | `/v1/doctor/appointments` | List appointments (existing) |
| GET | `/v1/doctor/appointments/{id}` | Get single appointment detail |
| PATCH | `/v1/doctor/appointments/{id}/notes` | Update private notes |
| PATCH | `/v1/doctor/appointments/{id}/status` | Update appointment status |

## Verification

- Frontend build: succeeds
- Frontend tests: 52 passed, 0 failures

## What Is Out Of Scope

- HAM-39 does not implement appointment creation or booking (HAM-30).
- HAM-39 does not implement patient appointment views (HAM-32).
- HAM-39 does not implement cancellation/rescheduling workflows beyond status update (HAM-36/37).
