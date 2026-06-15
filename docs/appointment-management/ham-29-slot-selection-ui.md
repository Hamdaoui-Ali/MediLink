# HAM-29 Appointment Slot Selection UI

HAM-29 implements the patient-facing page for viewing and selecting available appointment slots for a specific doctor.

## What Changed

- Created `PatientSlotSelectionPage` at `/patient/slots/:doctorId` with:
  - Date picker to choose a date (min date = today).
  - "Show Slots" button that fetches available slots from the backend.
  - Visual grid of available time slots as selectable buttons.
  - Selected slot highlighted with blue styling.
  - "Book Selected Slot" button that navigates to the booking confirmation page with pre-filled data (doctorId, date, startTime, endTime as query params).
  - Loading state during slot fetch.
  - Empty state when no slots are available.
  - Error handling for 401/403.
- Added `getDoctorSlots(doctorId, date)` method to `AppointmentService`.
- Added `Slot` interface to the shared appointment model.
- Added `/patient/slots/:doctorId` route protected by `authGuard` with `PATIENT` role.
