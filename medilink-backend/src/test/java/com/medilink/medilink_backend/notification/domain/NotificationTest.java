package com.medilink.medilink_backend.notification.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NotificationTest {

	@Test
	void constructorSetsDefaultStatusToPending() {
		Notification notification = new Notification(1L, 10L, NotificationType.APPOINTMENT_REMINDER,
				"patient@medilink.local", "Reminder subject");

		assertEquals(NotificationStatus.PENDING, notification.getStatus());
		assertEquals(1L, notification.getUserId());
		assertEquals(10L, notification.getAppointmentId());
		assertEquals(NotificationType.APPOINTMENT_REMINDER, notification.getType());
		assertEquals("patient@medilink.local", notification.getRecipientEmail());
		assertEquals("Reminder subject", notification.getSubject());
		assertNull(notification.getSentAt());
	}

	@Test
	void timestampsAreNotSetOutsideJpaContext() {
		Notification notification = new Notification(1L, null, NotificationType.APPOINTMENT_CONFIRMATION,
				"test@medilink.local", "Confirmation");

		assertNull(notification.getCreatedAt());
		assertNull(notification.getUpdatedAt());
	}

	@Test
	void markSentUpdatesStatusAndSetsSentAt() {
		Notification notification = new Notification(1L, 5L, NotificationType.APPOINTMENT_REMINDER,
				"test@medilink.local", "Reminder");

		notification.markSent();

		assertEquals(NotificationStatus.SENT, notification.getStatus());
		assertNotNull(notification.getSentAt());
	}

	@Test
	void markFailedUpdatesStatusToFailed() {
		Notification notification = new Notification(1L, 5L, NotificationType.APPOINTMENT_REMINDER,
				"test@medilink.local", "Reminder");

		notification.markFailed();

		assertEquals(NotificationStatus.FAILED, notification.getStatus());
		assertNull(notification.getSentAt());
	}

	@Test
	void cancelUpdatesStatusToCancelled() {
		Notification notification = new Notification(1L, 5L, NotificationType.APPOINTMENT_REMINDER,
				"test@medilink.local", "Reminder");

		notification.cancel();

		assertEquals(NotificationStatus.CANCELLED, notification.getStatus());
	}

	@Test
	void appointmentIdCanBeNull() {
		Notification notification = new Notification(1L, null, NotificationType.APPOINTMENT_CONFIRMATION,
				"test@medilink.local", "System notification");

		assertNull(notification.getAppointmentId());
		assertEquals(NotificationStatus.PENDING, notification.getStatus());
	}

	@Test
	void supportsAllNotificationTypes() {
		for (NotificationType type : NotificationType.values()) {
			Notification notification = new Notification(1L, 1L, type,
					"test@medilink.local", "Test " + type);

			assertEquals(type, notification.getType());
			assertEquals(NotificationStatus.PENDING, notification.getStatus());
		}
	}
}
