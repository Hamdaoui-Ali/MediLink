package com.medilink.medilink_backend.appointment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "patients")
public class PatientRefEntity {

	@Id
	private Long id;

	@Column(name = "user_id", nullable = false, updatable = false)
	private Long userId;

	protected PatientRefEntity() {}

	public Long getId() {
		return id;
	}

	public Long getUserId() {
		return userId;
	}
}
