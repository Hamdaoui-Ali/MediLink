# Database Workspace

This directory stores database assets that support the MediLink schema and future migrations.

Current contents:

- `mysql/001_initial_schema.sql` initial MySQL schema for the MVP entities
- migration notes and seed data notes as the project evolves

The current backend foundation targets MySQL in the `mysql` Spring profile and uses an in-memory placeholder database in the default `local` profile so the project can start before schema work begins.

Schema decisions are documented in `docs/database-schema.md`.

Runtime migrations live in `medilink-backend/src/main/resources/db/migration`.
