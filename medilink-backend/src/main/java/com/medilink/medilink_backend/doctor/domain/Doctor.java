package com.medilink.medilink_backend.doctor.domain;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "specialty_id", nullable = false)
	private Specialty specialty;

	@Column(columnDefinition = "TEXT")
	private String biography;

	@Column(name = "consultation_duration_minutes", nullable = false)
	private int consultationDurationMinutes = 30;

	@Column(name = "clinic_address", length = 500)
	private String clinicAddress;

	@Column(nullable = false, length = 30)
	private String status = "ACTIVE";

	protected Doctor() {}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Specialty getSpecialty() {
		return specialty;
	}

	public String getSpecialtyName() {
		return specialty != null ? specialty.getName() : null;
	}

	public String getBiography() {
		return biography;
	}

	public int getConsultationDurationMinutes() {
		return consultationDurationMinutes;
	}

	public String getClinicAddress() {
		return clinicAddress;
	}

	public String getStatus() {
		return status;
	}

	public boolean isActive() {
		return "ACTIVE".equals(status);
	}

	public void updateProfile(String biography, String clinicAddress, int consultationDurationMinutes) {
		this.biography = biography;
		this.clinicAddress = clinicAddress;
		this.consultationDurationMinutes = consultationDurationMinutes;
	}
}
