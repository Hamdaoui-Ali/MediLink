package com.medilink.medilink_backend.administration.dashboard.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "appointments")
public class AppointmentDashboard {

	@Id
	private Long id;

	@Column(name = "doctor_id", insertable = false, updatable = false)
	private Long doctorId;

	@Column(name = "patient_id", insertable = false, updatable = false)
	private Long patientId;

	@Column(name = "appointment_date", insertable = false, updatable = false)
	private LocalDate appointmentDate;

	@Column(name = "start_time", insertable = false, updatable = false)
	private LocalTime startTime;

	@Column(name = "end_time", insertable = false, updatable = false)
	private LocalTime endTime;

	@Column(insertable = false, updatable = false)
	private String status;

	@Column(insertable = false, updatable = false)
	private String reason;

	@Column(name = "created_at", insertable = false, updatable = false)
	private Instant createdAt;

	protected AppointmentDashboard() {}

	public Long getId() {
		return id;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public Long getPatientId() {
		return patientId;
	}

	public LocalDate getAppointmentDate() {
		return appointmentDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getStatus() {
		return status;
	}

	public String getReason() {
		return reason;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}
}
