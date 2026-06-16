# HAM-45 Manual QA Test Guide

Manual test steps for verifying MVP user journeys.

## Prerequisites
- Backend running: `cd medilink-backend && mvnw.cmd spring-boot:run`
- Frontend running: `cd medilink-frontend && npm start`
- MySQL database configured with migrations applied.

## Admin Journey

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as admin (admin@medilink.local / Admin@12345) | Redirected to admin dashboard |
| 2 | Navigate to specialty management | See list of existing specialties |
| 3 | Create a new specialty | Specialty appears in list |
| 4 | Edit a specialty name | Updated in list |
| 5 | Deactivate a specialty | Status changes to Inactive |

## Doctor Journey

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Login as doctor (doctor@medilink.local / Doctor@123) | Redirected to doctor dashboard |
| 2 | View profile page | Shows doctor details |
| 3 | Edit profile | Changes saved |
| 4 | Navigate to blocked slots | Empty list or existing slots |
| 5 | Create a blocked slot | Appears in list |
| 6 | Delete a blocked slot | Removed from list |
| 7 | View appointments | Shows patient bookings |
| 8 | Click an appointment | Detail panel with notes |
| 9 | Update notes | Notes saved with confirmation |
| 10 | Change status to Completed | Status updates |

## Patient Journey

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Register as patient | Redirected to login |
| 2 | Login as patient | Redirected to patient dashboard |
| 3 | Navigate to "Find a Doctor" | Doctor list or search bar |
| 4 | Search for a doctor | Matching doctors shown |
| 5 | Click "View Available Slots" | Date picker page |
| 6 | Select date, click "Show Slots" | Available time slots displayed |
| 7 | Click a slot, then "Book Selected Slot" | Booking confirmation page |
| 8 | Enter reason, click "Confirm Booking" | Success confirmation |
| 9 | Navigate to "My Appointments" | Shows booked appointment with CONFIRMED status |
| 10 | Cancel an appointment | Confirmation dialog, then cancelled status |
| 11 | Use the "Book an Appointment" direct form | Manual booking with doctor ID |

## Cross-Role Verification

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Log in as patient, try `/admin` URL | Redirected or 403 |
| 2 | Log in as doctor, try `/patient` URL | Redirected or 403 |
| 3 | Log out, try `/patient/appointments` | Redirected to login |
