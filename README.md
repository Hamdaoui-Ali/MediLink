# MediLink Workspace

MediLink is a doctor-patient appointment management platform focused on a reliable MVP: patient booking, doctor availability management, appointment tracking, and admin-managed platform data.

## Stack

- Backend: Spring Boot, Java 21
- Frontend: Angular 21
- Database target: MySQL
- Architecture: modular monolith with domain-driven boundaries

## Workspace Structure

- `medilink-backend/` Spring Boot API foundation
- `medilink-frontend/` Angular application foundation
- `database/` database notes and future SQL assets
- `docs/` shared project documentation

## Local Development

### Backend

Default profile: `local`

Commands:

```powershell
cd medilink-backend
.\mvnw.cmd spring-boot:run
```

Useful profiles:

- `local` starts with an in-memory placeholder database so the API can boot immediately
- `mysql` uses `MEDILINK_DB_URL`, `MEDILINK_DB_USERNAME`, and `MEDILINK_DB_PASSWORD`

Health endpoint:

- `GET http://localhost:8080/api/v1/health`

### Frontend

Commands:

```powershell
cd medilink-frontend
npm.cmd install
npm.cmd start
```

Development API base URL:

- `http://localhost:8080/api`

## Conventions

Branch naming:

- `feature/<linear-id>-short-description`
- `fix/<linear-id>-short-description`
- `chore/<short-description>`

Commit style:

- `feat:`
- `fix:`
- `chore:`
- `docs:`
- `refactor:`
- `test:`

Recommended example:

- `feat: add backend health endpoint`
