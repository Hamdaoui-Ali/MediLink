package com.medilink.medilink_backend.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "appointment_id")
	private Long appointmentId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 50)
	private NotificationType type;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private NotificationStatus status;

	@Column(name = "recipient_email", nullable = false, length = 190)
	private String recipientEmail;

	@Column(nullable = false, length = 255)
	private String subject;

	@Column(name = "sent_at")
	private Instant sentAt;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Notification() {}

	public Notification(Long userId, Long appointmentId, NotificationType type,
			String recipientEmail, String subject) {
		this.userId = userId;
		this.appointmentId = appointmentId;
		this.type = type;
		this.status = NotificationStatus.PENDING;
		this.recipientEmail = recipientEmail;
		this.subject = subject;
	}

	public Long getId() { return id; }
	public Long getUserId() { return userId; }
	public Long getAppointmentId() { return appointmentId; }
	public NotificationType getType() { return type; }
	public NotificationStatus getStatus() { return status; }
	public String getRecipientEmail() { return recipientEmail; }
	public String getSubject() { return subject; }
	public Instant getSentAt() { return sentAt; }
	public Instant getCreatedAt() { return createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }

	public void markSent() {
		this.status = NotificationStatus.SENT;
		this.sentAt = Instant.now();
	}

	public void markFailed() {
		this.status = NotificationStatus.FAILED;
	}

	public void cancel() {
		this.status = NotificationStatus.CANCELLED;
	}

	@PrePersist
	void onPersist() {
		Instant now = Instant.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = Instant.now();
	}
}
