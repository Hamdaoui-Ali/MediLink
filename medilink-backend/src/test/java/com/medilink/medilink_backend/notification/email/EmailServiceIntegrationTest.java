package com.medilink.medilink_backend.notification.email;

import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationStatus;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("local")
class EmailServiceIntegrationTest {

	@Autowired
	private EmailService emailService;

	@Autowired
	private NotificationRepository notificationRepository;

	@Test
	void notificationProcessingMarksFailedWhenNoMailServerAvailable() {
		Notification notification = new Notification(1L, 10L,
				NotificationType.APPOINTMENT_REMINDER,
				"test@medilink.local", "Test reminder");
		notificationRepository.save(notification);

		emailService.processNotification(notification);

		Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
		assertEquals(NotificationStatus.FAILED, updated.getStatus());
	}

	@Test
	void emailServiceBeanIsAvailable() {
		assertNotNull(emailService);
	}
}
