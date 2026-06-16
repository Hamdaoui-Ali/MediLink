package com.medilink.medilink_backend.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class DoctorRef {

	@Id
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	@Column(nullable = false, length = 30)
	private String status;

	@Column(name = "consultation_duration_minutes", nullable = false)
	private Integer consultationDurationMinutes;

	protected DoctorRef() {}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public String getStatus() {
		return status;
	}

	public boolean isActive() {
		return "ACTIVE".equals(status);
	}
}
