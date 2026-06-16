# HAM-6 - Initialize Spring Boot Backend Project

## What This Issue Was About

This issue created the first working backend for MediLink. The backend is the server side of the application: it will later handle login, appointments, patients, doctors, and admin actions.

Right now, the backend exists as a foundation. It starts, responds to a basic health check, and has the structural pieces needed for later features.

## What Was Added

- A Spring Boot application entry point.
- A clear package structure for future domains like identity, doctor, patient, appointment, administration, and notifications.
- A `local` startup profile that uses an in-memory database so the project can boot without external setup.
- A `mysql` profile for the real database target.
- A health endpoint that can confirm the backend is alive.
- A standard API response format so all future endpoints can return data in a consistent way.
- A global error handler so validation issues and unexpected failures are returned in a structured form.
- A basic security setup that keeps most endpoints protected by default.

## What This Means In Practice

For a non-developer, this means the backend is no longer just an empty project. It can already answer, "Am I running?" and it already has the rules for how future API responses should look.

The backend currently listens on port `8888`, and the health check is available at:

`GET http://localhost:8888/api/v1/health`

The response is wrapped in a standard structure that includes:

- whether the request succeeded
- the payload data
- an error object when something goes wrong
- a timestamp

## Where To Look

- `medilink-backend/src/main/java/com/medilink/medilink_backend/MedilinkBackendApplication.java` - backend startup class
- `medilink-backend/src/main/java/com/medilink/medilink_backend/identity/web/HealthController.java` - health endpoint
- `medilink-backend/src/main/java/com/medilink/medilink_backend/shared/api/ApiResponse.java` - standard API envelope
- `medilink-backend/src/main/java/com/medilink/medilink_backend/shared/exception/GlobalExceptionHandler.java` - consistent error handling
- `medilink-backend/src/main/java/com/medilink/medilink_backend/shared/config/SecurityConfig.java` - default access rules
- `medilink-backend/src/main/resources/application.yaml` - base server configuration
- `medilink-backend/src/main/resources/application-local.yaml` - local startup profile
- `medilink-backend/src/main/resources/application-mysql.yaml` - MySQL profile

## What Is Still Not Built

- Login and registration
- Appointment scheduling
- Doctor and patient management
- Admin dashboards
- Real database migrations and seed data

This issue gave the project a working backend foundation, but it did not yet deliver the product features described in the MediLink specification.
