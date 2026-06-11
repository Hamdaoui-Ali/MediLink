package com.medilink.medilink_backend.doctor.domain;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.identity.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "doctors")
public class Doctor {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false, unique = true)
	private User user;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "specialty_id", nullable = false)
	private Specialty specialty;

	@Lob
	@Column(columnDefinition = "TEXT")
	private String biography;

	@Column(name = "consultation_duration_minutes", nullable = false)
	private int consultationDurationMinutes;

	@Column(name = "clinic_address", length = 500)
	private String clinicAddress;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private DoctorStatus status;

	protected Doctor() {
	}

	public Doctor(
			User user,
			Specialty specialty,
			String biography,
			int consultationDurationMinutes,
			String clinicAddress
	) {
		this.user = user;
		this.specialty = specialty;
		this.biography = biography;
		this.consultationDurationMinutes = consultationDurationMinutes;
		this.clinicAddress = clinicAddress;
		this.status = DoctorStatus.ACTIVE;
	}

	public Long getId() {
		return id;
	}

	public User getUser() {
		return user;
	}

	public Specialty getSpecialty() {
		return specialty;
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

	public DoctorStatus getStatus() {
		return status;
	}

	public void update(Specialty specialty, String biography, int consultationDurationMinutes, String clinicAddress) {
		this.specialty = specialty;
		this.biography = biography;
		this.consultationDurationMinutes = consultationDurationMinutes;
		this.clinicAddress = clinicAddress;
	}

	public void activate() {
		this.status = DoctorStatus.ACTIVE;
		this.user.activate();
	}

	public void deactivate() {
		this.status = DoctorStatus.INACTIVE;
		this.user.deactivate();
	}
}
