# HAM-9 - Configure Database Migrations And Seed Data

## What This Issue Was About

HAM-9 made the database setup repeatable. Before this issue, the schema existed as a SQL design file and could be run manually in MySQL Workbench. After this issue, the backend can track database versions and apply seed data automatically when running with the MySQL profile.

For non-developers, this means MediLink no longer depends only on someone manually copying SQL into Workbench. The backend now knows how to prepare the database in a controlled and repeatable way.

## What Was Delivered

- Flyway was configured for the backend MySQL profile.
- A schema migration was added at `medilink-backend/src/main/resources/db/migration/V1__initial_mvp_schema.sql`.
- A seed migration was added at `medilink-backend/src/main/resources/db/migration/V2__seed_reference_data.sql`.
- Flyway MySQL support was added to the backend Maven dependencies.
- The MySQL profile was configured to baseline an existing database safely.
- Seed data was inserted into your local `medilink` database.
- Documentation was added at `docs/database-migrations.md`.

## What Flyway Does

Flyway is a database migration tool. It keeps a history table inside the database named `flyway_schema_history`.

That table records which migration files have already run. This prevents the same database change from being applied again by mistake.

In this project, Flyway reads migration files from:

`medilink-backend/src/main/resources/db/migration`

The current migrations are:

- `V1__initial_mvp_schema.sql` creates the MVP schema.
- `V2__seed_reference_data.sql` inserts initial roles, sample specialties, and a local admin account.

## Why Baseline Was Needed

You had already created the `medilink` database and manually added all HAM-8 tables in MySQL Workbench.

If Flyway tried to run `V1__initial_mvp_schema.sql` on that database, it would fail because the tables already existed.

To handle that safely, the MySQL profile uses:

`baseline-on-migrate: true`

This tells Flyway: if the database is not empty and Flyway has not tracked it yet, treat the existing schema as version `1`.

That is why your database now shows:

- version `1`: `Existing HAM-8 schema`
- version `2`: `seed reference data`

This keeps your manual work and brings the database under Flyway tracking from this point forward.

## Seed Data Added

The seed migration inserts the required roles:

- `ADMIN`
- `DOCTOR`
- `PATIENT`

It inserts sample specialties:

- General Medicine
- Cardiology
- Dermatology
- Pediatrics
- Orthopedics

It also inserts a local admin user:

- Email: `admin@medilink.local`
- Development password: `Admin@12345`
- Status: `ACTIVE`

The password is stored as a BCrypt hash, not plain text.

## Technical Explanation

The backend already had `spring-boot-starter-flyway` and `flyway-core`. HAM-9 added `flyway-mysql`, which is required for Flyway to support MySQL database-specific behavior.

Flyway is enabled only in `application-mysql.yaml`. It remains disabled in the local H2 profile because the migration SQL is MySQL-specific and uses MySQL syntax such as generated columns and `ENGINE=InnoDB`.

The MySQL profile now includes:

- `enabled: true` so Flyway runs with the MySQL profile
- `locations: classpath:db/migration` so Flyway knows where migrations live
- `baseline-on-migrate: true` so the existing Workbench-created database can be adopted
- `baseline-version: 1` so HAM-8 schema is treated as version 1
- `validate-on-migrate: true` so Flyway checks migration consistency
- `clean-disabled: true` so Flyway cannot wipe the database accidentally

The seed migration uses `INSERT IGNORE`. This makes seed data safer for repeated local setup because duplicate role names, specialty names, or admin email values will not break the migration if they already exist.

## Why These Choices Were Made

Flyway was chosen because it is already part of the Spring Boot backend setup and is a standard way to manage database migrations in Java applications.

The migration files live inside the backend resources folder because Spring Boot can package and run them automatically from the classpath.

The manual schema file in `database/mysql/001_initial_schema.sql` was kept as a reference because it is useful for understanding and reviewing the schema outside of application startup.

The runtime migration copy exists under `medilink-backend/src/main/resources/db/migration` because Flyway expects migration files there.

The local admin is inserted now so future authentication work can test login immediately after auth is implemented. It cannot be used for login yet because authentication endpoints are not built yet.

## What Happened In Your Local Database

The MySQL CLI was found at:

`C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe`

The database was reachable with your local `root/root` credentials.

The database already had the HAM-8 tables, so Flyway baselined version `1` and applied version `2`.

After verification, the database contained:

- `flyway_schema_history`
- roles: `ADMIN`, `DOCTOR`, `PATIENT`
- sample specialties
- local admin user: `admin@medilink.local`

## Files Added Or Updated

- `medilink-backend/pom.xml`
- `medilink-backend/src/main/resources/application-mysql.yaml`
- `medilink-backend/src/main/resources/db/migration/V1__initial_mvp_schema.sql`
- `medilink-backend/src/main/resources/db/migration/V2__seed_reference_data.sql`
- `docs/database-migrations.md`
- `docs/database-schema.md`
- `database/README.md`
- `docs/project-foundation/README.md`

## Verification

The normal backend test suite passed:

`cmd /c mvnw.cmd test`

The MySQL-profile backend test also passed using your local database:

`SPRING_PROFILES_ACTIVE=mysql`

`MEDILINK_DB_USERNAME=root`

`MEDILINK_DB_PASSWORD=root`

Flyway reported:

- 3 migrations validated
- current schema version is `2`
- schema `medilink` is up to date
- no migration necessary after seed data was applied

## What Is Out Of Scope

HAM-9 does not implement login or authentication.

HAM-9 does not create business APIs for users, doctors, patients, or appointments.

HAM-9 does not seed sample doctors or patients. It only seeds the minimum platform data needed for later development: roles, specialties, and a local admin.

HAM-9 does not require MySQL Workbench after the database is under Flyway tracking. Workbench can still be used to inspect data, but future schema changes should be added as new Flyway migration files.
