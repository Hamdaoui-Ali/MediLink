package com.medilink.medilink_backend.notification.email;

import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationStatus;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class EmailServiceTest {

	private final JavaMailSender mailSender = mock(JavaMailSender.class);
	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final EmailService emailService = new EmailService(
			mailSender, notificationRepository, "noreply@medilink.local");

	@Test
	void sendEmailSendsMessageViaMailSender() {
		EmailMessage message = new EmailMessage("patient@test.com", "Subject", "Body");

		emailService.sendEmail(message);

		verify(mailSender).send(any(SimpleMailMessage.class));
	}

	@Test
	void processNotificationMarksSentOnSuccess() {
		Notification notification = new Notification(1L, 10L,
				NotificationType.APPOINTMENT_REMINDER, "patient@test.com", "Reminder");

		emailService.processNotification(notification);

		assertEquals(NotificationStatus.SENT, notification.getStatus());
		verify(notificationRepository).save(notification);
	}

	@Test
	void processNotificationMarksFailedOnMailError() {
		Notification notification = new Notification(1L, 10L,
				NotificationType.APPOINTMENT_REMINDER, "patient@test.com", "Reminder");
		doThrow(new MailSendException("Connection refused")).when(mailSender)
				.send(any(SimpleMailMessage.class));

		emailService.processNotification(notification);

		assertEquals(NotificationStatus.FAILED, notification.getStatus());
		verify(notificationRepository).save(notification);
	}

	@Test
	void processNotificationSetsSentAtWhenSuccessful() {
		Notification notification = new Notification(1L, 10L,
				NotificationType.APPOINTMENT_CONFIRMATION, "patient@test.com", "Confirmed");

		emailService.processNotification(notification);

		assertEquals(NotificationStatus.SENT, notification.getStatus());
		assertEquals(true, notification.getSentAt() != null);
	}
}
