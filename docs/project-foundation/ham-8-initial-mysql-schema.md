# HAM-8 - Design Initial MySQL Database Schema

## What This Issue Was About

HAM-8 created the first complete database design for the MediLink MVP. The goal was to define where the application will store users, doctors, patients, appointments, schedules, blocked slots, notifications, and audit history.

For non-developers, this is the data blueprint of the platform. It decides what information MediLink needs to remember and how different pieces of information connect to each other.

## What Was Delivered

- A MySQL schema design file was added at `database/mysql/001_initial_schema.sql`.
- A detailed schema explanation was added at `docs/database-schema.md`.
- The schema includes all MVP entities required by the Notion specification.
- Relationships were defined between users, roles, doctors, patients, specialties, appointments, notifications, and audit logs.
- Database-level rules were added to prevent invalid or duplicate data where possible.
- Appointment double-booking prevention was included through a generated key and unique constraint.

## The Main Tables In Simple Terms

`roles` stores the high-level type of account: Admin, Doctor, or Patient.

`users` stores shared login and account information, such as name, email, password hash, phone number, and account status.

`specialties` stores medical specialties such as Cardiology, Dermatology, or Pediatrics.

`doctors` stores doctor-specific profile data and connects each doctor to a user account and specialty.

`patients` stores patient-specific profile data and connects each patient to a user account.

`doctor_availability` stores the normal weekly working hours that doctors define.

`blocked_slots` stores specific unavailable time periods that should not be bookable.

`appointments` stores bookings between patients and doctors.

`notifications` stores email confirmation and reminder records.

`audit_logs` stores important system actions for traceability.

## Technical Explanation

The schema uses MySQL with `InnoDB`, `utf8mb4`, and `utf8mb4_unicode_ci`. `InnoDB` is used because the application needs foreign keys and transaction-safe behavior. `utf8mb4` is used so names and text fields can store modern characters safely.

Most tables use `BIGINT AUTO_INCREMENT` primary keys. This keeps IDs simple while leaving enough room for growth.

Foreign keys are used to make relationships explicit. For example:

- `users.role_id` points to `roles.id`.
- `doctors.user_id` points to `users.id`.
- `doctors.specialty_id` points to `specialties.id`.
- `appointments.doctor_id` points to `doctors.id`.
- `appointments.patient_id` points to `patients.id`.

Unique constraints protect important business rules:

- user emails must be unique
- specialty names must be unique
- one user can only have one doctor profile
- one user can only have one patient profile
- one doctor cannot have two active appointments for the same active slot

Check constraints protect status fields and time ranges. Examples:

- appointment status must be one of `CONFIRMED`, `CANCELLED`, `COMPLETED`, `MISSED`, or `RESCHEDULED`
- appointment start time must be before appointment end time
- availability start time must be before availability end time

## Why These Choices Were Made

The Notion specification says MediLink should be a modular monolith using a domain-driven approach. The database follows the same idea by separating the main business areas into clear tables instead of putting everything into one large table.

User accounts were separated from doctor and patient profiles because every person logs in as a user, but doctors and patients need different profile data. This avoids duplication and keeps future role-based access easier to implement.

Appointments use automatic confirmation because the Notion specification says appointments should not require manual doctor approval in the MVP. A booked valid slot becomes `CONFIRMED` immediately.

The `active_slot_key` generated column was added to support double-booking prevention at the database level. For active appointment states, MySQL generates a slot key from date and start time. A unique constraint on doctor plus active slot prevents the same doctor from being booked twice at the same time.

Cancelled and rescheduled appointments do not generate an active slot key. This lets the system keep appointment history without blocking a future valid booking.

## What The Schema Protects

- Duplicate emails are blocked.
- Duplicate role names are blocked.
- Duplicate specialty names are blocked.
- Invalid statuses are blocked.
- Invalid time ranges are blocked.
- A doctor cannot be double-booked for the same active appointment slot.
- A user cannot accidentally have multiple doctor profiles or multiple patient profiles.

## What Still Belongs In Backend Logic

Some rules are too complex for the database alone and should be implemented in backend services later:

- checking that appointments are not in the past
- generating slots based on consultation duration
- rejecting overlapping availability ranges
- rejecting conflicting blocked slots
- hiding blocked slots from patient search
- enforcing doctor-only and patient-only access rules
- making sure doctors only see their own appointments
- making sure patients only see their own appointments

The schema gives the backend a strong foundation, but it does not replace business logic.

## Files Added Or Updated

- `database/mysql/001_initial_schema.sql`
- `docs/database-schema.md`
- `database/README.md`

## Verification

The SQL was reviewed against the HAM-8 acceptance criteria and the Notion specification. Later, during HAM-9, the same schema was copied into a Flyway migration and verified against the local MySQL `medilink` database.

## What Is Out Of Scope

HAM-8 did not automatically run the schema on application startup. That was intentionally left for HAM-9, which introduced Flyway migrations and seed data.
