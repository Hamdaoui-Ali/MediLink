# MediLink Initial Database Schema

This document explains the first MySQL schema for the MediLink MVP. It follows the Notion specification: admins manage the platform, doctors manage availability and appointments, patients search and book appointments, and the system sends confirmations and reminders.

HAM-8 is a schema design issue. The reference SQL lives in `database/mysql/001_initial_schema.sql`. Runtime Flyway migrations for HAM-9 live in `medilink-backend/src/main/resources/db/migration`.

## Main Design Choices

- MySQL is the database target because the Notion specification names MySQL as the project database.
- Each main business concept has its own table: users, roles, doctors, patients, specialties, appointments, availability, blocked slots, notifications, and audit logs.
- User accounts are separated from doctor and patient profiles. This keeps login information in one place while allowing each role to have its own profile details.
- Appointments are automatically confirmed by default, matching the MVP booking model in the Notion specification.
- Appointment double-booking prevention is supported at database level through a unique active slot key.
- Important statuses are stored as constrained strings so the database rejects unknown values.

## Tables

### `roles`

Stores the available account roles, such as Admin, Doctor, and Patient.

Key fields:

- `id` - primary key
- `name` - unique role name
- `description` - optional explanation of the role

Why it exists:

The application needs role-based access control. A user must be connected to one role so the system can decide what that user is allowed to do.

### `users`

Stores shared account information for every person who can log in.

Key fields:

- `id` - primary key
- `role_id` - links the user to `roles`
- `full_name` - person name shown in the app
- `email` - unique login identifier
- `password_hash` - securely hashed password, not plain text
- `phone_number` - optional contact number
- `account_status` - Active, Inactive, or Disabled

Why it exists:

Admins, doctors, and patients all need core account data. Keeping that data in one table avoids duplication.

### `specialties`

Stores medical specialties or departments, such as cardiology or dermatology.

Key fields:

- `id` - primary key
- `name` - unique specialty name
- `description` - optional explanation
- `status` - Active or Inactive

Why it exists:

Patients need to search doctors by specialty, and admins need to manage the list of specialties.

### `doctors`

Stores doctor-specific profile information.

Key fields:

- `id` - primary key
- `user_id` - links the doctor profile to a user account
- `specialty_id` - links the doctor to a specialty
- `biography` - profile text
- `consultation_duration_minutes` - slot duration used for scheduling
- `clinic_address` - doctor location
- `status` - Active or Inactive

Why it exists:

A doctor has scheduling and profile information that normal users do not have. This table keeps those details separate from login details.

### `patients`

Stores patient-specific profile information.

Key fields:

- `id` - primary key
- `user_id` - links the patient profile to a user account
- `date_of_birth` - optional patient profile field
- `gender` - optional profile field
- `address` - optional profile field

Why it exists:

Patients need their own profile and appointment history without mixing those details into the shared users table.

### `doctor_availability`

Stores recurring weekly working hours for doctors.

Key fields:

- `id` - primary key
- `doctor_id` - links availability to a doctor
- `day_of_week` - day number from 1 to 7
- `start_time` and `end_time` - available time range
- `is_active` - allows ranges to be disabled without deleting history

Why it exists:

Doctors define when they normally work. The application uses this table to generate available appointment slots.

Important constraint:

The database checks that `start_time` is before `end_time`.

### `blocked_slots`

Stores one-off unavailable periods for doctors.

Key fields:

- `id` - primary key
- `doctor_id` - links the blocked slot to a doctor
- `block_date` - exact date of the block
- `start_time` and `end_time` - blocked time range
- `reason` - optional explanation
- `is_active` - allows a block to be disabled

Why it exists:

A doctor may normally work on a day but still need to block time for vacation, meetings, emergencies, or other conflicts.

Important constraint:

Active exact duplicate blocks for the same doctor are prevented. Overlapping time-range validation should still be handled by backend business logic.

### `appointments`

Stores bookings between patients and doctors.

Key fields:

- `id` - primary key
- `doctor_id` - doctor for the appointment
- `patient_id` - patient who booked it
- `appointment_date` - appointment date
- `start_time` and `end_time` - booked slot
- `status` - Confirmed, Cancelled, Completed, Missed, or Rescheduled
- `reason` - why the patient booked the appointment
- `doctor_notes` - private doctor notes after the visit

Why it exists:

This is the central table for the booking lifecycle.

Double-booking prevention:

The schema defines an `active_slot_key` generated by MySQL. For appointments with `CONFIRMED`, `COMPLETED`, or `MISSED` status, the key combines the appointment date and start time. A unique constraint on `doctor_id` and `active_slot_key` prevents two active appointments from using the same doctor slot.

Cancelled and rescheduled appointments do not generate an active slot key, so they can remain as historical records without blocking future valid bookings.

### `notifications`

Stores confirmation and reminder records.

Key fields:

- `id` - primary key
- `user_id` - recipient user
- `appointment_id` - related appointment when applicable
- `type` - confirmation, reminder, cancellation, or reschedule notice
- `status` - Pending, Sent, Failed, or Cancelled
- `recipient_email` - email destination
- `subject` - email subject
- `sent_at` - when the message was sent

Why it exists:

The Notion specification requires appointment confirmations and reminders. This table gives the system a place to track those messages.

### `audit_logs`

Stores important platform actions.

Key fields:

- `id` - primary key
- `user_id` - user who performed the action, when known
- `action` - what happened
- `entity_type` and `entity_id` - what record was affected
- `description` - optional human-readable detail
- `created_at` - when the action happened

Why it exists:

Audit logs help admins understand important actions such as account changes, appointment cancellations, and status updates.

## Relationship Summary

- One role can be assigned to many users.
- One user can have one doctor profile.
- One user can have one patient profile.
- One specialty can be linked to many doctors.
- One doctor can have many availability ranges.
- One doctor can have many blocked slots.
- One doctor can have many appointments.
- One patient can have many appointments.
- One appointment can have many notifications.
- Audit logs can point back to the user who performed an action.

## What The Database Protects

- Duplicate role names are not allowed.
- Duplicate user emails are not allowed.
- Duplicate specialty names are not allowed.
- A user can only have one doctor profile.
- A user can only have one patient profile.
- Appointment start time must be before appointment end time.
- Availability start time must be before availability end time.
- Blocked slot start time must be before blocked slot end time.
- Appointment statuses must match the MVP lifecycle.
- Confirmed, completed, and missed appointments cannot double-book the same doctor slot.

## What The Backend Still Needs To Enforce

Some appointment rules are too business-specific to rely on database constraints alone.

The backend should still enforce:

- patients cannot book appointments in the past
- appointment times must match the doctor's consultation duration
- generated slots must come from active doctor availability
- blocked slots must be excluded from patient-visible availability
- overlapping availability ranges should be rejected
- overlapping blocked slots should be rejected when they create unclear schedules
- patients should only see their own appointments
- doctors should only see their own appointments

The database supports these rules, but the backend must still apply the full business logic.
