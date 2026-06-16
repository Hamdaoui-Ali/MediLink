# MediLink MVP — Manual Test & Verification Document

## Prerequisites

### Start the Services

```bash
# Terminal 1: Backend (port 1234, context-path /api)
cd medilink-backend
set MEDILINK_JWT_SECRET=local-dev-jwt-secret-key-32chars
./mvnw.cmd spring-boot:run

# Terminal 2: Frontend (port 4200)
cd medilink-frontend
npm start
```

### Base URLs

| Service | URL |
|---------|-----|
| Backend API | `http://localhost:1234/api` |
| Frontend App | `http://localhost:4200` |
| Swagger UI | `http://localhost:1234/api/swagger-ui.html` |

### Seed Accounts

| Role | Email | Password | Notes |
|------|-------|----------|-------|
| Admin | `admin@medilink.local` | `Admin@12345` | Auto-created by migration |
| Doctor | *created by admin* | — | Use admin panel to create |
| Patient | *self-registered* | — | Register at `/register` |

### Context-Path Note

All API calls in this document use the **full path** including `/api` prefix (e.g., `http://localhost:1234/api/v1/health`). The Swagger UI and frontend automatically include this.

---

## Complete API Reference

| # | Method | Endpoint | Role | Body / Params | Response |
|---|--------|----------|------|--------------|----------|
| H1 | GET | `/api/v1/health` | public | — | `{"success":true,"data":{"status":"UP","service":"medilink-backend",...}}` |
| A1 | POST | `/api/v1/auth/login` | public | `{"email":"...","password":"..."}` | `{"success":true,"data":{"accessToken":"...","tokenType":"Bearer","user":{...}}}` |
| A2 | GET | `/api/v1/auth/me` | any | Header: `Authorization: Bearer <token>` | `{"success":true,"data":{"id":...,"fullName":"...","email":"...","role":"...",...}}` |
| R1 | POST | `/api/v1/patients/register` | public | `{"fullName":"...","email":"...","password":"...","phoneNumber":"...","dateOfBirth":"...","gender":"...","address":"..."}` | `{"success":true,"data":{"userId":...,"patientId":...,...}}` (201) |
| D1 | GET | `/api/v1/doctor/profile` | DOCTOR | — | `{"success":true,"data":{"id":...,"fullName":"...","specialtyName":"...",...}}` |
| D2 | PATCH | `/api/v1/doctor/profile` | DOCTOR | `{"biography":"...","clinicAddress":"...","phoneNumber":"...","consultationDurationMinutes":30}` | `{"success":true,"data":{...}}` |
| P1 | GET | `/api/v1/patient/doctors` | PATIENT | `?specialtyId=1` or `?name=Dr` (optional) | `{"success":true,"data":[{"id":...,"fullName":"...","specialtyName":"...",...}]}` |
| P2 | GET | `/api/v1/patient/doctors/{id}` | PATIENT | — | `{"success":true,"data":{"id":...,"fullName":"...","specialtyName":"...",...}}` |
| P3 | GET | `/api/v1/patient/doctors/{id}/slots?date=YYYY-MM-DD` | PATIENT | — | `{"success":true,"data":[{"startTime":"09:00:00","endTime":"09:30:00"},...]}` |
| P4 | POST | `/api/v1/patient/appointments` | PATIENT | `{"doctorId":...,"appointmentDate":"...","startTime":"...","reason":"..."}` | `{"success":true,"data":{"id":...,"status":"CONFIRMED",...}}` |
| P5 | GET | `/api/v1/patient/appointments` | PATIENT | `?filter=upcoming` or `?filter=past` (optional) | `{"success":true,"data":[{...}]}` |
| P6 | GET | `/api/v1/patient/appointments/{id}` | PATIENT | — | `{"success":true,"data":{...}}` |
| P7 | PATCH | `/api/v1/patient/appointments/{id}/cancel` | PATIENT | — | `{"success":true,"data":{"status":"CANCELLED",...}}` |
| P8 | PATCH | `/api/v1/patient/appointments/{id}/reschedule?newDate=...&newStartTime=...` | PATIENT | — | `{"success":true,"data":{"status":"CONFIRMED",...}}` |
| DR1 | GET | `/api/v1/doctor/appointments` | DOCTOR | `?status=CONFIRMED&from=...&to=...` (optional) | `{"success":true,"data":[{...}]}` |
| DR2 | GET | `/api/v1/doctor/appointments/{id}` | DOCTOR | — | `{"success":true,"data":{...}}` |
| DR3 | PATCH | `/api/v1/doctor/appointments/{id}/notes` | DOCTOR | `{"notes":"..."}` | `{"success":true,"data":{...}}` |
| DR4 | PATCH | `/api/v1/doctor/appointments/{id}/status` | DOCTOR | `{"status":"COMPLETED"}` | `{"success":true,"data":{...}}` |
| B1 | GET | `/api/v1/doctor/blocked-slots` | DOCTOR | — | `{"success":true,"data":[{...}]}` |
| B2 | POST | `/api/v1/doctor/blocked-slots` | DOCTOR | `{"blockDate":"...","startTime":"...","endTime":"...","reason":"..."}` | `{"success":true,"data":{...}}` (201) |
| B3 | PATCH | `/api/v1/doctor/blocked-slots/{id}` | DOCTOR | same as POST | `{"success":true,"data":{...}}` |
| B4 | DELETE | `/api/v1/doctor/blocked-slots/{id}` | DOCTOR | — | `{"success":true,"data":null}` (204) |
| AD1 | GET | `/api/v1/specialties` | public/ADMIN | `?activeOnly=true` (default) | `{"success":true,"data":[{...}]}` |
| AD2 | GET | `/api/v1/specialties/{id}` | ADMIN | — | `{"success":true,"data":{...}}` |
| AD3 | POST | `/api/v1/specialties` | ADMIN | `{"name":"...","description":"..."}` | `{"success":true,"data":{...}}` (201) |
| AD4 | PUT | `/api/v1/specialties/{id}` | ADMIN | same as POST | `{"success":true,"data":{...}}` |
| AD5 | PATCH | `/api/v1/specialties/{id}/activate` | ADMIN | — | `{"success":true,"data":{...}}` |
| AD6 | PATCH | `/api/v1/specialties/{id}/deactivate` | ADMIN | — | `{"success":true,"data":{...}}` |

---

## Flow 1: Admin Journey

### Test A1 — Login as Admin

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A1.1 | POST `/api/v1/auth/login` | `{"email":"admin@medilink.local","password":"Admin@12345"}` | HTTP 200, `success:true`, `data.role:"ADMIN"`. Save the `accessToken` as `ADMIN_TOKEN`. |

### Test A2 — Verify Admin Identity

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A2.1 | GET `/api/v1/auth/me` | Header: `Authorization: Bearer $ADMIN_TOKEN` | HTTP 200, `data.email:"admin@medilink.local"`, `data.role:"ADMIN"`, `data.accountStatus:"ACTIVE"` |

### Test A3 — List Specialties (Public)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A3.1 | GET `/api/v1/specialties?activeOnly=true` | No auth needed | HTTP 200, `data` array with 5 specialties (General Medicine, Cardiology, Dermatology, Pediatrics, Orthopedics) |

### Test A4 — Create a Specialty

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A4.1 | POST `/api/v1/specialties` | Header: `Authorization: Bearer $ADMIN_TOKEN`<br>`{"name":"Neurology","description":"Brain and nervous system care"}` | HTTP 201, `success:true`, `data.name:"Neurology"`, `data.status:"ACTIVE"`. Save `data.id` as `SPECIALTY_ID`. |

### Test A5 — Edit a Specialty

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A5.1 | PUT `/api/v1/specialties/$SPECIALTY_ID` | Header: `Authorization: Bearer $ADMIN_TOKEN`<br>`{"name":"Neurology","description":"Updated description"}` | HTTP 200, `data.description:"Updated description"` |

### Test A6 — Deactivate / Activate

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A6.1 | PATCH `/api/v1/specialties/$SPECIALTY_ID/deactivate` | Header: `Authorization: Bearer $ADMIN_TOKEN` | HTTP 200, `data.status:"INACTIVE"` |
| A6.2 | PATCH `/api/v1/specialties/$SPECIALTY_ID/activate` | Header: `Authorization: Bearer $ADMIN_TOKEN` | HTTP 200, `data.status:"ACTIVE"` |

### Test A7 — Duplicate Specialty Name (Error)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A7.1 | POST `/api/v1/specialties` | Same body as A4.1 | HTTP 409, `success:false`, `error.code:"DUPLICATE_SPECIALTY_NAME"` |

### Test A8 — Admin Cannot Access Doctor/Patient Endpoints

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| A8.1 | GET `/api/v1/doctor/appointments` | Header: `Authorization: Bearer $ADMIN_TOKEN` | HTTP 403, `error.code:"ACCESS_DENIED"` |
| A8.2 | GET `/api/v1/patient/appointments` | Header: `Authorization: Bearer $ADMIN_TOKEN` | HTTP 403 |

---

## Flow 2: Doctor Journey

### Test B1 — Create Doctor Account (via Admin)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B1.1 | Register as a doctor (no public endpoint — use DB insert or admin panel) | *Use SQL or future admin UI* | Doctor account created |

*For manual testing, use this SQL:*
```sql
INSERT INTO users (role_id, full_name, email, password_hash, account_status)
VALUES (2, 'Dr. Test', 'doctor@medilink.local', '<BCRYPT_HASH>', 'ACTIVE');
INSERT INTO doctors (user_id, specialty_id, biography, consultation_duration_minutes, clinic_address, status)
VALUES (<NEW_USER_ID>, 1, 'Experienced general practitioner', 30, '123 Clinic Street', 'ACTIVE');
```

### Test B2 — Login as Doctor

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B2.1 | POST `/api/v1/auth/login` | `{"email":"doctor@medilink.local","password":"Doctor@123"}` | HTTP 200, `data.user.role:"DOCTOR"`. Save `accessToken` as `DOCTOR_TOKEN`. |

### Test B3 — View Doctor Profile

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B3.1 | GET `/api/v1/doctor/profile` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 200, `data.fullName:"Dr. Test"`, `data.specialtyName:"General Medicine"`, `data.consultationDurationMinutes:30` |

### Test B4 — Update Doctor Profile

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B4.1 | PATCH `/api/v1/doctor/profile` | Header: `Authorization: Bearer $DOCTOR_TOKEN`<br>`{"biography":"Updated bio","clinicAddress":"456 New Street","consultationDurationMinutes":45}` | HTTP 200, `data.biography:"Updated bio"`, `data.consultationDurationMinutes:45` |

### Test B5 — Create Blocked Slots

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B5.1 | POST `/api/v1/doctor/blocked-slots` | Header: `Authorization: Bearer $DOCTOR_TOKEN`<br>`{"blockDate":"2026-08-15","startTime":"10:00:00","endTime":"12:00:00","reason":"Conference"}` | HTTP 201, `data.reason:"Conference"`. Save `data.id` as `BLOCKED_ID`. |
| B5.2 | GET `/api/v1/doctor/blocked-slots` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 200, `data[0].reason:"Conference"` |

### Test B6 — Delete Blocked Slot

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B6.1 | DELETE `/api/v1/doctor/blocked-slots/$BLOCKED_ID` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 204 |
| B6.2 | GET `/api/v1/doctor/blocked-slots` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 200, `data:[]` (empty) |

### Test B7 — View Doctor Appointments

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B7.1 | GET `/api/v1/doctor/appointments` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 200, `data` array (may be empty initially) |
| B7.2 | GET `/api/v1/doctor/appointments?status=CONFIRMED&from=2026-08-01&to=2026-08-31` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 200, filtered results |

### Test B8 — Update Appointment Notes & Status

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B8.1 | PATCH `/api/v1/doctor/appointments/$APPT_ID/notes` | Header: `Authorization: Bearer $DOCTOR_TOKEN`<br>`{"notes":"Patient vitals normal"}` | HTTP 200, `data.doctorNotes:"Patient vitals normal"` |
| B8.2 | PATCH `/api/v1/doctor/appointments/$APPT_ID/status` | Header: `Authorization: Bearer $DOCTOR_TOKEN`<br>`{"status":"COMPLETED"}` | HTTP 200, `data.status:"COMPLETED"` |

### Test B9 — Invalid Status Transition (Error)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B9.1 | PATCH `/api/v1/doctor/appointments/$APPT_ID/status` | Header: `Authorization: Bearer $DOCTOR_TOKEN`<br>`{"status":"CONFIRMED"}` (already COMPLETED) | HTTP 400, `error.code:"INVALID_STATUS_TRANSITION"` |

### Test B10 — Doctor Cannot Access Patient Endpoints

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| B10.1 | GET `/api/v1/patient/appointments` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 403 |
| B10.2 | POST `/api/v1/patient/appointments` | Header: `Authorization: Bearer $DOCTOR_TOKEN` | HTTP 403 |

---

## Flow 3: Patient Journey

### Test C1 — Register as Patient

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C1.1 | POST `/api/v1/patients/register` | `{"fullName":"Jane Patient","email":"jane@test.com","password":"Patient@123","phoneNumber":"+15551234567","dateOfBirth":"1995-06-15","gender":"FEMALE","address":"100 Care Street"}` | HTTP 201, `success:true`, `data.role:"PATIENT"` |

### Test C2 — Duplicate Email Registration (Error)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C2.1 | POST `/api/v1/patients/register` | Same as C1.1 | HTTP 409, `error.code:"DUPLICATE_EMAIL"` |

### Test C3 — Login as Patient

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C3.1 | POST `/api/v1/auth/login` | `{"email":"jane@test.com","password":"Patient@123"}` | HTTP 200, `data.user.role:"PATIENT"`. Save `accessToken` as `PATIENT_TOKEN`. |

### Test C4 — Search for Doctors

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C4.1 | GET `/api/v1/patient/doctors` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data` array with active doctors. Save one `data[X].id` as `DOCTOR_ID`. |
| C4.2 | GET `/api/v1/patient/doctors?name=Test` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, filtered to doctors with "Test" in name |
| C4.3 | GET `/api/v1/patient/doctors?specialtyId=1` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, only General Medicine doctors |
| C4.4 | GET `/api/v1/patient/doctors/$DOCTOR_ID` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, full doctor detail |

### Test C5 — View Available Slots (No Availability)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C5.1 | GET `/api/v1/patient/doctors/$DOCTOR_ID/slots?date=2026-08-15` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data:[]` (empty — doctor has no availability set) |

### Test C6 — Add Doctor Availability (via SQL) and Retry

*Insert availability via SQL:*
```sql
-- Monday (1) availability 9:00-17:00
INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, is_active)
VALUES (<DOCTOR_ID>, 1, '09:00:00', '17:00:00', TRUE);
```

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C6.1 | GET `/api/v1/patient/doctors/$DOCTOR_ID/slots?date=2026-08-17` (Monday) | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data` array with 16 slots (9:00-9:30 through 16:30-17:00, 30-min intervals) |

### Test C7 — Book an Appointment

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C7.1 | POST `/api/v1/patient/appointments` | Header: `Authorization: Bearer $PATIENT_TOKEN`<br>`{"doctorId":$DOCTOR_ID,"appointmentDate":"2026-08-17","startTime":"10:00:00","reason":"Annual checkup"}` | HTTP 200, `data.status:"CONFIRMED"`, `data.reason:"Annual checkup"`. Save `data.id` as `APPT_ID`. |

### Test C8 — Duplicate Booking (Conflict)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C8.1 | POST `/api/v1/patient/appointments` | Same as C7.1 | HTTP 409, `error.code:"SLOT_UNAVAILABLE"` |

### Test C9 — Past Date Booking (Error)

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C9.1 | POST `/api/v1/patient/appointments` | `{"doctorId":$DOCTOR_ID,"appointmentDate":"2020-01-01","startTime":"10:00:00","reason":"Test"}` | HTTP 400 |

### Test C10 — List My Appointments

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C10.1 | GET `/api/v1/patient/appointments` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data[0].status:"CONFIRMED"`, `data[0].reason:"Annual checkup"` |
| C10.2 | GET `/api/v1/patient/appointments?filter=upcoming` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, only future appointments |
| C10.3 | GET `/api/v1/patient/appointments?filter=past` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, only past appointments |

### Test C11 — Get Single Appointment

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C11.1 | GET `/api/v1/patient/appointments/$APPT_ID` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data.id` matches `$APPT_ID` |

### Test C12 — Cancel Appointment

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C12.1 | PATCH `/api/v1/patient/appointments/$APPT_ID/cancel` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data.status:"CANCELLED"` |

### Test C13 — Reschedule Appointment

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C13.1 | Book a new appointment first (C7.1 with different time) | — | Get new `APPT_ID2` |
| C13.2 | PATCH `/api/v1/patient/appointments/$APPT_ID2/reschedule?newDate=2026-08-24&newStartTime=14:00:00` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 200, `data.appointmentDate:"2026-08-24"`, `data.startTime:"14:00:00"`, `data.status:"CONFIRMED"` |

### Test C14 — Patient Cannot Access Doctor Endpoints

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| C14.1 | GET `/api/v1/doctor/appointments` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 403 |
| C14.2 | PATCH `/api/v1/doctor/appointments/1/status` | Header: `Authorization: Bearer $PATIENT_TOKEN` | HTTP 403 |

---

## Flow 4: Unauthenticated Access

### Test D1 — All Protected Endpoints Return 401

| Step | Endpoint | Method | Expected |
|------|----------|--------|----------|
| D1.1 | `/api/v1/doctor/appointments` | GET | HTTP 401 |
| D1.2 | `/api/v1/patient/appointments` | GET | HTTP 401 |
| D1.3 | `/api/v1/patient/doctors` | GET | HTTP 401 |
| D1.4 | `/api/v1/patient/doctors/1/slots?date=2026-08-01` | GET | HTTP 401 |
| D1.5 | `/api/v1/patient/appointments` | POST | HTTP 401 |
| D1.6 | `/api/v1/doctor/profile` | GET | HTTP 401 |

### Test D2 — Public Endpoints Work Without Auth

| Step | Endpoint | Method | Expected |
|------|----------|--------|----------|
| D2.1 | `/api/v1/health` | GET | HTTP 200, `data.status:"UP"` |
| D2.2 | `/api/v1/specialties?activeOnly=true` | GET | HTTP 200 |
| D2.3 | `/api/v1/auth/login` (valid creds) | POST | HTTP 200 |
| D2.4 | `/api/v1/patients/register` | POST | HTTP 201 |

### Test D3 — Invalid Login

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| D3.1 | POST `/api/v1/auth/login` | `{"email":"jane@test.com","password":"WrongPassword"}` | HTTP 401, `error.code:"INVALID_CREDENTIALS"` |

---

## Flow 5: Notification Verification

### Test E1 — Confirmation Notification on Booking

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| E1.1 | Book an appointment (C7.1) | — | HTTP 200 |
| E1.2 | Query DB: `SELECT * FROM notifications WHERE appointment_id = $APPT_ID AND type = 'APPOINTMENT_CONFIRMATION'` | — | 1 row exists, `status = 'PENDING'` |

### Test E2 — Scheduled Reminder Creation

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| E2.1 | Query DB: `SELECT * FROM notifications WHERE appointment_id = $APPT_ID AND type = 'APPOINTMENT_REMINDER'` | — | 1 row exists (if appointment is today or tomorrow), `status = 'PENDING'` |

### Test E3 — Cancelled Appointments Skip Reminders

| Step | Action | Input | Expected Output |
|------|--------|-------|-----------------|
| E3.1 | Verify cancelled appointment has no reminder: `SELECT * FROM notifications WHERE appointment_id = <CANCELLED_APPT_ID> AND type = 'APPOINTMENT_REMINDER'` | — | 0 rows |

---

## Flow 6: UI Verification (Frontend)

Run the application and navigate to `http://localhost:4200`.

### Home Page
- [ ] MediLink branding visible
- [ ] Login and Register links present

### Registration
- [ ] Navigate to `/register`
- [ ] Fill all fields and submit
- [ ] Redirected to `/login`
- [ ] Duplicate email shows error

### Login
- [ ] Navigate to `/login`
- [ ] Login with patient credentials
- [ ] Redirected to patient dashboard

### Patient Dashboard
- [ ] Three links visible: "Find a Doctor", "Book an Appointment", "My Appointments"

### Find a Doctor
- [ ] Navigate to `/patient/doctors`
- [ ] Doctor cards display with name, specialty, bio
- [ ] Search bar filters by name
- [ ] "View Available Slots" link works

### Slot Selection
- [ ] Navigate to `/patient/slots/:doctorId`
- [ ] Date picker allows selecting future dates
- [ ] "Show Slots" loads available time slots
- [ ] Slots display as selectable buttons
- [ ] Selecting a slot highlights it
- [ ] "Book Selected Slot" navigates to confirmation

### Booking Confirmation
- [ ] Doctor, date, and time pre-filled from slot selection
- [ ] Reason textarea accepts input
- [ ] "Confirm Booking" submits
- [ ] Success confirmation card displays with appointment details
- [ ] "Book Another Appointment" resets form

### My Appointments
- [ ] Navigate to `/patient/appointments`
- [ ] Tabs: All / Upcoming / Past
- [ ] Appointment cards with status badges
- [ ] "Cancel Appointment" shows confirmation dialog
- [ ] Cancelling updates the appointment

### Error Handling
- [ ] 401: Expired token redirects to login
- [ ] 403: Cross-role access shows permission error
- [ ] 409: Duplicate booking shows conflict message

---

## Automated Test Verification

Run the full backend test suite as a final check:

```bash
cd medilink-backend
./mvnw.cmd test
# Expected: Tests run: 183, Failures: 0, Errors: 0, Skipped: 0
```

---

## Sign-Off Checklist

| Area | Pass/Fail | Notes |
|------|-----------|-------|
| Admin login | ☐ | |
| Specialty CRUD | ☐ | |
| Doctor login | ☐ | |
| Doctor profile view/edit | ☐ | |
| Blocked slots CRUD | ☐ | |
| Doctor appointment view/notes/status | ☐ | |
| Patient registration | ☐ | |
| Patient login | ☐ | |
| Doctor search (all, by name, by specialty) | ☐ | |
| Slot generation (available slots) | ☐ | |
| Slot generation (blocked slots excluded) | ☐ | |
| Slot generation (booked slots excluded) | ☐ | |
| Appointment booking | ☐ | |
| Duplicate booking rejection (409) | ☐ | |
| Past date rejection (400) | ☐ | |
| Patient appointment list | ☐ | |
| Patient appointment list (upcoming/past filters) | ☐ | |
| Appointment cancellation | ☐ | |
| Appointment rescheduling | ☐ | |
| Confirmation notification creation | ☐ | |
| Reminder notification creation | ☐ | |
| Unauthenticated access (401) | ☐ | |
| Cross-role access blocked (403) | ☐ | |
| Public endpoints accessible | ☐ | |
| Invalid login returns 401 | ☐ | |
| Invalid status transition returns 400 | ☐ | |
| Frontend doctor search UI | ☐ | |
| Frontend slot selection UI | ☐ | |
| Frontend booking confirmation UI | ☐ | |
| Frontend appointment dashboard UI | ☐ | |
| Frontend cancellation with confirmation | ☐ | |
| Automated tests (183/183) | ☐ | |

---

**Document Version:** 1.0 | **Date:** June 2026 | **Project:** MediLink MVP
