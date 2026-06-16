# HAM-42 Scheduled Appointment Checks

HAM-42 implements a scheduled backend process that identifies upcoming confirmed appointments and creates notification tracking records for them.

## What Changed

- Created `Notification` JPA entity mapping to the existing `notifications` table with `@PrePersist`/`@PreUpdate` lifecycle hooks, status management methods (`markSent()`, `markFailed()`, `cancel()`), and a constructor for creating pending notification records.
- Created `NotificationType` enum (`APPOINTMENT_CONFIRMATION`, `APPOINTMENT_REMINDER`, `APPOINTMENT_CANCELLATION`, `APPOINTMENT_RESCHEDULE_NOTICE`).
- Created `NotificationStatus` enum (`PENDING`, `SENT`, `FAILED`, `CANCELLED`).
- Created `NotificationRepository` with `existsByAppointmentIdAndType` for idempotency checks and `findByAppointmentIdAndType` for existing record lookups.
- Created `AppointmentScheduler` service with `@Scheduled(fixedRate = 30 * 60 * 1000)`:
  - Runs every 30 minutes to check for upcoming appointments.
  - Queries confirmed appointments for today and tomorrow.
  - Skips appointments that already have an `APPOINTMENT_REMINDER` notification (idempotent).
  - Resolves the patient's `user_id` from the `patients` table for each appointment.
  - Creates `Notification` records with `PENDING` status and `APPOINTMENT_REMINDER` type.
- Created `SchedulingConfig` with `@EnableScheduling` to enable Spring's scheduling support.
- Added `findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc` to `AppointmentRepository` for unscoped confirmed appointment queries.

## Architecture

```
medilink-backend/src/main/java/com/medilink/medilink_backend/
  notification/
    domain/
      Notification.java          # JPA entity for notifications table
      NotificationType.java      # Enum: APPOINTMENT_REMINDER, etc.
      NotificationStatus.java    # Enum: PENDING, SENT, FAILED, CANCELLED
    repository/
      NotificationRepository.java # JPA repo with idempotency checks
    scheduler/
      AppointmentScheduler.java  # @Scheduled service (every 30 min)
  shared/config/
    SchedulingConfig.java        # @EnableScheduling configuration
  appointment/repository/
    AppointmentRepository.java   # Added findByStatusAndAppointmentDateBetween
```

## Test Coverage

| Test | Tests | Type |
|------|-------|------|
| `AppointmentSchedulerIntegrationTest` | 5 | Integration - real scheduler execution against H2 |

### Test cases:
1. Creates notification records for today and tomorrow confirmed appointments
2. Skips appointments that already have existing reminders
3. Skips cancelled and completed appointments
4. Creates records with PENDING status
5. Skips appointments outside the today+tomorrow window

## What Is Out Of Scope

- HAM-42 does not send emails (covered by a later issue, likely HAM-43).
- HAM-42 does not support configurable checkpoint hours (currently hardcoded to today+tomorrow).
- HAM-42 does not generate different notification types (only `APPOINTMENT_REMINDER`).
- The 1-hour checkpoint mentioned in the tasks is not yet implemented.
