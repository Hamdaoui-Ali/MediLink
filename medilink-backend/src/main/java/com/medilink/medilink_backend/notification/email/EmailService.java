package com.medilink.medilink_backend.notification.email;

import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationStatus;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	private final JavaMailSender mailSender;
	private final NotificationRepository notificationRepository;
	private final String fromAddress;

	public EmailService(
			JavaMailSender mailSender,
			NotificationRepository notificationRepository,
			@Value("${medilink.notifications.from-address:noreply@medilink.local}") String fromAddress
	) {
		this.mailSender = mailSender;
		this.notificationRepository = notificationRepository;
		this.fromAddress = fromAddress;
	}

	public void sendEmail(EmailMessage message) {
		try {
			SimpleMailMessage mailMessage = new SimpleMailMessage();
			mailMessage.setFrom(fromAddress);
			mailMessage.setTo(message.to());
			mailMessage.setSubject(message.subject());
			mailMessage.setText(message.body());
			mailSender.send(mailMessage);
			log.info("Email sent to {}", message.to());
		} catch (MailException e) {
			log.error("Failed to send email to {}: {}", message.to(), e.getMessage());
			throw e;
		}
	}

	public void sendTestEmail(String to) {
		sendEmail(new EmailMessage(to, "MediLink Test Email",
				"This is a test email from MediLink."));
	}

	public void processNotification(Notification notification) {
		try {
			sendEmail(new EmailMessage(
					notification.getRecipientEmail(),
					notification.getSubject(),
					"Appointment #" + notification.getAppointmentId() + " - " + notification.getType()
			));
			notification.markSent();
		} catch (MailException e) {
			notification.markFailed();
		}
		notificationRepository.save(notification);
	}
}
