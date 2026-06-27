# Sprint 2 UI QA Checklist

## Prerequisites

- Backend running at `http://localhost:1234/api`
- Frontend running at `http://localhost:4200` (`npm start`)
- At least one admin account seeded (`admin@medilink.local` / `Admin@12345`)
- At least one doctor account created via admin panel
- At least one patient account registered

---

## 1. Local Setup Sanity Check

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 1.1 | Open `http://localhost:4200` in browser | App loads, shows MediLink header, sidebar with "Welcome to MediLink", nav links: Home, Sign In, Create Account | |
| 1.2 | Check browser console (F12) | No Angular or runtime errors | |
| 1.3 | Resize browser to 375px width (mobile) | Layout adapts: hamburger menu appears, sidebar collapses below content | |

---

## 2. Guest Navigation

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 2.1 | Open app as guest (not signed in) | Nav shows: Home, Sign In, Create Account | |
| 2.2 | Click "Home" | Redirects to `/`, shows hero section with role-based feature cards | |
| 2.3 | Click "Sign In" | Navigates to `/login`, shows sign-in form with email/password fields, hero panel, "Create patient account" link | |
| 2.4 | Click "Create Account" | Navigates to `/register`, shows registration form with all fields | |
| 2.5 | Try navigating to `/patient` directly | Redirected to `/login?returnUrl=/patient` (guard working) | |
| 2.6 | Try navigating to `/doctor` directly | Redirected to `/login?returnUrl=/doctor` (guard working) | |
| 2.7 | Try navigating to `/admin` directly | Redirected to `/login?returnUrl=/admin` (guard working) | |
| 2.8 | Home page content | Shows product copy (not "Project foundation" or dev wording). No `localhost:1234` visible. | |

---

## 3. Sign In / Sign Out

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 3.1 | Submit empty login form | Shows validation errors: "Enter a valid email address" and "Enter your password" | |
| 3.2 | Submit with invalid credentials | Shows "The email or password is incorrect" error | |
| 3.3 | Submit with valid patient credentials | Redirects to `/patient` (patient dashboard) | |
| 3.4 | Sign in as admin | Redirects to `/admin` (admin dashboard) | |
| 3.5 | Sign in as doctor | Redirects to `/doctor` (doctor dashboard) | |
| 3.6 | Verify nav after sign in | Nav shows role-specific links + "Logout" button. No guest links (Sign In, Create Account) visible. | |
| 3.7 | Active nav link highlighted | Current page link has `is-active` styling (dark teal background) | |
| 3.8 | Click "Logout" | Sign out, redirected to `/login`, nav shows guest links again | |
| 3.9 | Backend unreachable during sign in | Shows "Unable to connect to the server" message (not `localhost:1234` URL) | |

---

## 4. Patient Dashboard

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 4.1 | Sign in as patient | Dashboard shows welcome with patient name, 3 action cards (Find a Doctor, My Appointments, Book Appointment) | |
| 4.2 | Click "Find a Doctor" | Navigates to `/patient/doctors` | |
| 4.3 | Click "My Appointments" | Navigates to `/patient/appointments` | |
| 4.4 | Click "Book Appointment" | Navigates to `/patient/book` (manual entry mode) | |
| 4.5 | Upcoming appointment (if exists) | Shows next appointment card with date, time, reason, status badge | |
| 4.6 | No upcoming appointments | Shows "You have no upcoming appointments" with "Find a Doctor" CTA | |
| 4.7 | Loading state | Shows "Loading your schedule..." while data loads | |
| 4.8 | Resize to 500px | Action cards stack in single column | |

---

## 5. Patient Doctor Search

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 5.1 | Navigate to `/patient/doctors` | Shows initial state: "Ready to find care" with guidance text | |
| 5.2 | Enter a doctor name and click Search | Shows loading state: "Finding doctors matching your search..." | |
| 5.3 | Results appear | Shows results count + redesigned cards with: doctor name, specialty badge, bio (if any), consultation duration, clinic address (if any), "View Available Slots" button | |
| 5.4 | No doctor ID shown as primary content | Cards show human-readable info, not raw IDs | |
| 5.5 | Click "View Available Slots" | Navigates to `/patient/slots/:doctorId` with query params (name, specialty, address, duration) | |
| 5.6 | Search with no results | Shows "No doctors found" with searched term + "Start a New Search" button | |
| 5.7 | Search with empty input | Search button disabled, does not fire | |
| 5.8 | "Clear search" link | Resets to initial state | |
| 5.9 | "Back to Dashboard" link | Returns to `/patient` | |
| 5.10 | Resize to 600px | Search bar stacks vertically, cards stack | |

---

## 6. Patient Slot Selection

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 6.1 | Navigate to slot page from doctor search | Shows step indicator: 1-Doctor (done), 2-Select Time (active), 3-Confirm | |
| 6.2 | Doctor context card | Shows doctor name, specialty badge, consultation duration, clinic address (from query params) | |
| 6.3 | No raw doctorId as heading | Page heading is "Book an Appointment", doctor name in context card | |
| 6.4 | Choose a date and click "Show Times" | Loading state: "Finding available appointment times..." | |
| 6.5 | Slots appear | Groups: Morning (before 12PM) and Afternoon (12PM+). Time buttons displayed in grid. | |
| 6.6 | Select a slot | Slot button highlights blue with scale animation. Header shows "Selected: 10:00 AM - 10:30 AM" | |
| 6.7 | Click selected slot again | Deselects (un-highlights) | |
| 6.8 | No slot selected | "Continue" button shows "Select a time to continue" and is disabled | |
| 6.9 | Slot selected | Continue button shows selected time: "Continue with 10:00 AM - 10:30 AM" | |
| 6.10 | Click Continue | Navigates to `/patient/book` with query params (doctorId, date, startTime, endTime, name, specialty, duration) | |
| 6.11 | Date with no slots | Shows "No available slots" with suggestion to try another date | |
| 6.12 | API error | Shows error state with retry button | |
| 6.13 | Resize to 500px | Date row stacks vertically, step labels hidden (numbers only) | |

---

## 7. Patient Booking Confirmation

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 7.1 | Arrive from slot selection | Step indicator: 3-Confirm (active). Confirmation summary card with: doctor name + specialty, date, time, reason textarea | |
| 7.2 | No raw doctorId as primary content | Doctor info shows name/specialty from query params, not ID numbers | |
| 7.3 | Reason input | Has visible label "Reason for Visit", form-help text, placeholder | |
| 7.4 | Submit with empty reason | "Confirm Appointment" button disabled | |
| 7.5 | Fill reason and submit | Button shows "Confirming your appointment..." during request | |
| 7.6 | Successful booking | Success state: green-tinted card, "Appointment Confirmed", full appointment details, status badge, "View My Appointments" button, "Book Another Appointment" button, "Back to Dashboard" link | |
| 7.7 | Click "View My Appointments" | Navigates to `/patient/appointments` | |
| 7.8 | Click "Book Another Appointment" | Resets form to initial state | |
| 7.9 | Conflict/409 error | Shows "This time slot is no longer available" message | |
| 7.10 | Session expired/401 error | Shows "Your session has expired" with guidance | |
| 7.11 | Navigate directly to `/patient/book` (no params) | Shows manual entry prompt with "Find a Doctor" and "Enter Details Manually" options | |
| 7.12 | Resize to 500px | Step labels hidden, success details stack vertically | |

---

## 8. Patient Appointments

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 8.1 | Navigate to `/patient/appointments` | Shows tab bar: All / Upcoming / Past, "Book New Appointment" button in header | |
| 8.2 | Click different tabs | Tab switches, list reloads with filtered results. Active tab highlighted blue. | |
| 8.3 | Appointment cards | Each card shows: date, status badge (global `badge-*` classes), time, reason. No "Doctor ID" raw display. No "Notes" section (doctor-private). | |
| 8.4 | Cancel appointment (CONFIRMED status) | "Cancel Appointment" button visible. Click shows confirmation: "Cancel this appointment?" with "Yes, Cancel" / "Keep It" | |
| 8.5 | Confirm cancel | Success message "Appointment cancelled." Status badge updates. | |
| 8.6 | Cancel error | Error message shown | |
| 8.7 | Cancelled/Completed/Missed appointments | No cancel button shown (non-cancellable statuses) | |
| 8.8 | No reschedule link | Reschedule action is not shown | |
| 8.9 | Empty appointments | Shows "No appointments yet" with "Find a Doctor" CTA | |
| 8.10 | Loading state | Shows "Loading your appointments..." | |
| 8.11 | Error state | Error message with "Try Again" button | |
| 8.12 | Resize to 500px | Cards stack, footer buttons stack vertically | |

---

## 9. Doctor Dashboard

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 9.1 | Sign in as doctor | Dashboard shows "Welcome, Dr. LastName", 3 action cards (Appointments, Blocked Slots, My Profile) | |
| 9.2 | Click "Appointments" | Navigates to `/doctor/appointments` | |
| 9.3 | Click "Blocked Slots" | Navigates to `/doctor/blocked-slots` | |
| 9.4 | Click "My Profile" | Navigates to `/doctor/profile` | |
| 9.5 | Profile incomplete prompt | If biography or clinic address missing, shows blue "Complete Your Profile" card with CTA | |
| 9.6 | Profile complete | Prompt card is hidden | |
| 9.7 | Today's schedule (if appointments exist) | Shows list with patient names, time, status badges | |
| 9.8 | No appointments today (but upcoming) | Shows next appointment date + count | |
| 9.9 | No appointments at all | Shows "No upcoming appointments" message | |
| 9.10 | Loading state | "Loading your schedule..." | |

---

## 10. Doctor Appointments Workspace

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 10.1 | Navigate to `/doctor/appointments` | Shows "My Appointments" with subtitle "Manage today and upcoming appointments", quick filter tabs (All, Confirmed, Completed, Cancelled), filters bar (status dropdown, date picker) | |
| 10.2 | Click quick tab (e.g. Confirmed) | Tab highlights blue, list filters to confirmed only | |
| 10.3 | Select an appointment from the table | Row highlights in blue. Detail panel appears on the right. | |
| 10.4 | Detail panel content | Shows: patient name, date, time, reason, status badge, valid status transitions, private notes section | |
| 10.5 | Status actions | Buttons for valid transitions (e.g. "Mark Completed", "Mark Cancelled"). Color-coded: green for success, gray for cancel, amber for missed, purple for reschedule, blue for confirm. | |
| 10.6 | Terminal appointment (completed/cancelled) | "This appointment is in a final state" message, no action buttons | |
| 10.7 | Private notes section | "Doctor only" badge visible. Placeholder says "visible only to you". | |
| 10.8 | Save notes | Type text, click "Save Notes". Shows "Notes saved." message. | |
| 10.9 | Update status | Click status action, confirmation message shown. Status updates in list and detail. | |
| 10.10 | Close detail panel | Returns to list-only view, "Select an appointment" placeholder shown | |
| 10.11 | Clear filters | Both status and date filters cleared, "Clear filters" button disappears | |
| 10.12 | Empty filter results | "No appointments match your filters." message | |
| 10.13 | Error state | Error message with retry button | |
| 10.14 | Keyboard navigation | Tab to table rows, row has visible focus outline. Enter to select. | |
| 10.15 | Resize to 900px | Grid becomes single column (list on top, detail below) | |

---

## 11. Doctor Blocked Slots

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 11.1 | Navigate to `/doctor/blocked-slots` | Shows "Blocked Slots" page with form and existing slots list | |
| 11.2 | Create a blocked slot | Fill date, start/end time, optional reason. Click "Block Slot". Success message shown, table updates. | |
| 11.3 | Validation | Empty required fields show "is required" errors | |
| 11.4 | Delete a blocked slot | "Remove" button shows as danger button. Click removes the slot. | |
| 11.5 | Empty list | "No blocked slots yet." message | |
| 11.6 | Loading state | "Loading blocked slots..." | |

---

## 12. Doctor Profile

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 12.1 | Navigate to `/doctor/profile` | Shows account info (name, email, specialty, status badges) + edit form | |
| 12.2 | Edit profile fields | Fill biography, clinic address, phone, duration. Click "Save Changes". | |
| 12.3 | Validation | Biography max 2000 chars, clinic address max 500 chars, duration min 5 min | |
| 12.4 | Success message | "Profile updated." message shown | |
| 12.5 | Error message | Error message shown on API failure | |
| 12.6 | Status badges | Active shows green badge, inactive shows gray badge | |

---

## 13. Admin Dashboard

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 13.1 | Sign in as admin | Navigates to `/admin` with role-specific nav | |
| 13.2 | Dashboard content | Shows admin-oriented cards/links (basic for now, enhanced in HAM-65) | |

---

## 14. Admin Specialty Management

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 14.1 | Navigate to `/admin/specialties` | Shows "Specialty Management" with subtitle explaining purpose, search input, status filter tabs (All, Active, Inactive), specialties table, create/edit form | |
| 14.2 | Search by name | Type partial name, table filters in real-time. Shows "N of M total" count. | |
| 14.3 | Search by description | Description matches also filter results | |
| 14.4 | Filter by status | Click "Active" / "Inactive" tabs, table filters. Tab highlights blue. | |
| 14.5 | Status badges | Active: green `badge-success`, Inactive: gray `badge-muted`. Labels are human-readable. | |
| 14.6 | Create specialty | Fill name + description, click "Create specialty". Success message. Table updates alphabetically. | |
| 14.7 | Edit specialty | Click "Edit" on a row. Form populates. Modify and save. | |
| 14.8 | Validation | Name required (max 120 chars), description (max 500 chars) | |
| 14.9 | Deactivate with confirmation | Click "Deactivate" on ACTIVE specialty. Shows "Confirm Deactivate" + "Cancel" buttons. Click confirm. Status changes to INACTIVE. | |
| 14.10 | Cancel deactivation | Click "Deactivate" then "Cancel". No change made. | |
| 14.11 | Activate | Click "Activate" on INACTIVE specialty. Status changes immediately (no confirmation needed). | |
| 14.12 | No specialties | "No specialties yet. Create your first specialty." message | |
| 14.13 | No filter matches | "No specialties match your filters. Try a different search." message | |
| 14.14 | Loading state | "Loading specialties..." | |
| 14.15 | Error state | Error message with retry button (shown above grid) | |
| 14.16 | Resize to 900px | Grid collapses: list on top, form below. Search bar stacks vertically. | |

---

## 15. Responsive / Mobile Check

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 15.1 | Test at 375px width | All pages render without horizontal overflow | |
| 15.2 | Tables on mobile | Tables have horizontal scroll, text remains readable | |
| 15.3 | Buttons on mobile | Touch targets at least 44px. No overlapping buttons. | |
| 15.4 | Forms on mobile | Inputs full width, labels visible, no cut-off content | |
| 15.5 | Cards on mobile | Cards stack vertically, action cards show single column | |
| 15.6 | Navigation on mobile | Hamburger menu visible. Tap opens vertical nav. Tap link closes menu. | |

---

## 16. Basic Accessibility Check

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 16.1 | Keyboard tab through all links/buttons | Focus outline visible (blue ring) on each element | |
| 16.2 | Keyboard navigate nav bar | Focus moves through nav links in order | |
| 16.3 | Keyboard select doctor appointment row | Table row focuses with visible outline, Enter selects | |
| 16.4 | Form labels | All visible inputs have associated labels (not just placeholders) | |
| 16.5 | Status understanding | Badges include text labels (e.g., "Confirmed", "Cancelled") — not color-only | |
| 16.6 | Error messages | Errors are text-based, shown with red backgrounds, include actionable guidance | |
| 16.7 | Mobile menu | Toggle button has `aria-label` and `aria-expanded` attributes | |
| 16.8 | Navigation landmarks | `<nav>` elements have `aria-label` attributes | |

---

## 17. Error, Empty, and Loading States (Cross-page)

| # | Test Step | Expected Result | Pass? |
|---|-----------|-----------------|-------|
| 17.1 | Stop backend, load a page | Error messages are user-friendly (no raw error codes or localhost URLs) | |
| 17.2 | Empty appointment lists | Shows contextual empty state with CTA | |
| 17.3 | Loading indicators | "Loading..." messages shown while data fetches | |
| 17.4 | Retry on error | "Try Again" or "Retry" buttons available on error states | |
| 17.5 | Invalid form submission | Validation errors shown inline, submit button disabled | |
| 17.6 | 401 Unauthorized response | Shows "Your session has expired. Please sign in again." consistently across all pages | |

---

## Notes

- **Date format**: All dates use locale-formatted display (e.g., "Mon, Jun 23, 2025"), not raw ISO strings
- **Time format**: All times show 12-hour format with AM/PM
- **Consistency**: All pages use "Sign In" (not "Login" or "Log in"), "Create Account" (not "Register")
- **Design tokens**: All colors use CSS variables from `styles.scss`. No hardcoded hex values in feature SCSS for colors.
- **Shared classes**: `.btn`, `.badge`, `.card`, `.message`, `.form-input`, `.form-label`, `.empty-state`, `.loading-state`, `.error-state` used across pages
