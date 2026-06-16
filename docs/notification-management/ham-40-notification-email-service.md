# HAM-40 Notification Model and Email Service

HAM-40 creates the email notification foundation for MediLink, including the email sending service, SMTP configuration, and notification processing logic.

## What Changed

- Added `spring-boot-starter-mail` dependency to `pom.xml` for JavaMailSender support.
- Created `application.yml` with SMTP configuration:
  - Configurable via environment variables (`MEDILINK_MAIL_HOST`, `MEDILINK_MAIL_PORT`, etc.)
  - Defaults to `localhost:1025` for local development (works with MailHog, Mailpit, etc.)
  - Short timeouts (5s) to fail fast when no mail server is available.
- Created `EmailMessage` record with `to`, `subject`, and `body` fields.
- Created `EmailService` with:
  - `sendEmail(EmailMessage)` — sends via `JavaMailSender`, logs success/failure.
  - `sendTestEmail(String to)` — sends a simple test email.
  - `processNotification(Notification)` — sends email for a notification and updates its status (SENT or FAILED).
- Notification entity, enums (NotificationType, NotificationStatus), and repository already exist from HAM-41 (booking confirmation flow) and HAM-42 (scheduled checks).

## Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/
  notification/
    domain/
      Notification.java           # Entity (from HAM-42)
      NotificationType.java       # Enum (from HAM-42)
      NotificationStatus.java     # Enum (from HAM-42)
    repository/
      NotificationRepository.java # JPA repo (from HAM-42)
    email/
      EmailService.java           # NEW: sends emails, processes notifications
      EmailMessage.java           # NEW: email DTO
    scheduler/
      AppointmentScheduler.java   # Scheduled checks (from HAM-42)
  resources/
    application.yml               # NEW: SMTP + JWT config
```

## Email Configuration

Environment variables for production:
| Variable | Default | Description |
|----------|---------|-------------|
| `MEDILINK_MAIL_HOST` | localhost | SMTP server host |
| `MEDILINK_MAIL_PORT` | 1025 | SMTP server port |
| `MEDILINK_MAIL_USERNAME` | (empty) | SMTP auth username |
| `MEDILINK_MAIL_PASSWORD` | (empty) | SMTP auth password |
| `MEDILINK_MAIL_AUTH` | false | Enable SMTP auth |
| `MEDILINK_MAIL_STARTTLS` | false | Enable STARTTLS |
| `MEDILINK_NOTIFICATION_FROM` | noreply@medilink.local | From address |

## Test Coverage

| Test | Tests | Type |
|------|-------|------|
| `EmailServiceTest` | 4 | Unit — mocked JavaMailSender |
| `EmailServiceIntegrationTest` | 2 | Integration — real Spring + no mail server = FAILED status |

### Test cases:
- `sendEmail` sends via mail sender
- `processNotification` marks SENT on success
- `processNotification` marks FAILED on mail error
- `processNotification` sets `sentAt` timestamp
- Integration: notification processing marks FAILED when no mail server

## What Is Out Of Scope

- HAM-40 does not wire the email service into the appointment booking flow (HAM-41 handles confirmation on booking).
- HAM-40 does not create HTML email templates (plain text only).
- HAM-40 does not process PENDING notifications automatically (the scheduler only creates records, doesn't send them).
