package com.medilink.medilink_backend.administration.dashboard.domain;

import com.medilink.medilink_backend.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class DoctorDashboard {

	@Id
	private Long id;

	@Column(name = "user_id", insertable = false, updatable = false)
	private Long userId;

	@Column(name = "specialty_id", insertable = false, updatable = false)
	private Long specialtyId;

	@Column(insertable = false, updatable = false)
	private String status;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	protected DoctorDashboard() {}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}

	public Long getSpecialtyId() {
		return specialtyId;
	}

	public String getStatus() {
		return status;
	}

	public String getFullName() {
		return user != null ? user.getFullName() : "Unknown";
	}
}
