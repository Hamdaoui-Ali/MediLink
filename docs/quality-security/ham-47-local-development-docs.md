# HAM-47 Local Development Documentation

## Prerequisites

| Tool | Version | Check |
|------|---------|-------|
| Java | 21+ | `java --version` |
| Node.js | 20+ | `node --version` |
| npm | 10+ | `npm --version` |
| MySQL | 8.0+ | `mysql --version` |
| Maven | Wrapper included | `cd medilink-backend && mvnw.cmd --version` |

## Quick Start

### 1. Database Setup

```sql
CREATE DATABASE medilink;
CREATE USER 'medilink'@'localhost' IDENTIFIED BY 'medilink';
GRANT ALL PRIVILEGES ON medilink.* TO 'medilink'@'localhost';
FLUSH PRIVILEGES;
```

Flyway migrations run automatically on startup. See `database/mysql/001_initial_schema.sql` for the full schema.

### 2. Environment Variables

Create a `.env` file or export these variables:

```bash
# JWT Authentication
export MEDILINK_JWT_SECRET="your-256-bit-secret-key-for-production"

# SMTP (optional for local dev - emails logged)
export MEDILINK_MAIL_HOST="localhost"
export MEDILINK_MAIL_PORT="1025"
export MEDILINK_MAIL_AUTH="false"
export MEDILINK_MAIL_STARTTLS="false"
export MEDILINK_NOTIFICATION_FROM="noreply@medilink.local"
```

### 3. Backend

```bash
cd medilink-backend

# Run with default profile (uses application.yml)
./mvnw.cmd spring-boot:run

# Or with specific profile
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=local

# API available at: http://localhost:8080/api/v1
# Swagger UI at: http://localhost:8080/api/swagger-ui.html
```

### 4. Frontend

```bash
cd medilink-frontend

# Install dependencies
npm install

# Start dev server
npm start

# Available at: http://localhost:4200
```

### 5. Running Tests

```bash
# Backend unit + integration tests
cd medilink-backend
./mvnw.cmd test

# Frontend tests
cd medilink-frontend
npx vitest run --globals --environment=jsdom
```

## Sample Test Accounts

After running the app with migrations, the following seed accounts exist:

| Role | Email | Password |
|------|-------|----------|
| Admin | `admin@medilink.local` | `Admin@12345` |

**Note**: Doctors and patients must be created through the application (admin creates doctors, patients self-register). Use these for testing:

```
Doctor: Register via admin panel
Patient: Register via /register page
```

## Project Structure

```
MediLink/
├── medilink-backend/        # Spring Boot 4.0.6 + Java 21
│   └── src/main/java/com/medilink/medilink_backend/
│       ├── administration/   # Admin: specialties
│       ├── appointment/      # Core: appointments, booking
│       ├── availability/     # Doctor availability + slots
│       ├── blockedslot/      # Doctor blocked times
│       ├── doctor/           # Doctor profiles + search
│       ├── identity/         # Auth: login, registration, JWT
│       ├── notification/     # Email + notification tracking
│       ├── patient/          # Patient registration
│       └── shared/           # Config, exceptions, API wrappers
├── medilink-frontend/        # Angular 21 + TypeScript
│   └── src/app/
│       ├── features/         # admin/, auth/, doctor/, patient/
│       └── shared/           # services/, models/, guards/
└── docs/                     # Documentation by domain
    ├── appointment-management/
    ├── notification-management/
    └── quality-security/
```
