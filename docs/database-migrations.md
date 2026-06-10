# MediLink Database Migrations And Seed Data

This document explains how HAM-9 configures database migrations and seed data.

## What Runs Automatically

Flyway is enabled in the backend `mysql` profile.

When the backend starts with the `mysql` profile, Flyway reads migration files from:

`medilink-backend/src/main/resources/db/migration`

Current migrations:

- `V1__initial_mvp_schema.sql` creates the MVP database schema.
- `V2__seed_reference_data.sql` inserts required roles, sample specialties, and a local admin account.

## Existing Database Handling

Your `medilink` database already contains the HAM-8 tables.

Because of that, the `mysql` profile uses:

`baseline-on-migrate: true`

This lets Flyway safely start tracking an existing non-empty database. On your current database, Flyway should create its schema history table, mark version `1` as already present, and then run version `2` for seed data.

## Fresh Database Handling

If the `medilink` database is empty, Flyway runs migrations from the beginning:

1. `V1__initial_mvp_schema.sql` creates all schema tables.
2. `V2__seed_reference_data.sql` inserts initial roles, sample specialties, and the local admin user.

This means the database can be recreated from scratch without manually copying SQL from Workbench.

## Seeded Data

The seed migration creates these roles:

- `ADMIN`
- `DOCTOR`
- `PATIENT`

It creates sample specialties:

- General Medicine
- Cardiology
- Dermatology
- Pediatrics
- Orthopedics

It creates one local admin account:

- Email: `admin@medilink.local`
- Development password: `Admin@12345`

The password is stored as a BCrypt hash, not as plain text.

## How To Run With MySQL

Set the backend to the `mysql` profile and make sure the connection points to your local MySQL database.

Default values in `application-mysql.yaml`:

- Database URL: `jdbc:mysql://localhost:3306/medilink`
- Username: `medilink`
- Password: `medilink`

If your Workbench database uses a different username or password, set:

- `MEDILINK_DB_URL`
- `MEDILINK_DB_USERNAME`
- `MEDILINK_DB_PASSWORD`

Then start the backend with the `mysql` profile.

## Important Notes

- Flyway `clean` is disabled so the application cannot wipe the database accidentally.
- Local H2 startup still keeps Flyway disabled because the migration SQL is MySQL-specific.
- Authentication is not implemented yet, so the local admin account is prepared for the later authentication issue but cannot log in until that feature exists.
