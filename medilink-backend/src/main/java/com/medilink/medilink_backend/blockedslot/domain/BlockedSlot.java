package com.medilink.medilink_backend.blockedslot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "blocked_slots")
public class BlockedSlot {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "doctor_id", nullable = false)
	private Long doctorId;

	@Column(name = "block_date", nullable = false)
	private LocalDate blockDate;

	@Column(name = "start_time", nullable = false)
	private LocalTime startTime;

	@Column(name = "end_time", nullable = false)
	private LocalTime endTime;

	@Column(length = 500)
	private String reason;

	@Column(name = "is_active", nullable = false)
	private boolean active = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected BlockedSlot() {}

	public BlockedSlot(Long doctorId, LocalDate blockDate, LocalTime startTime, LocalTime endTime, String reason) {
		this.doctorId = doctorId;
		this.blockDate = blockDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.reason = reason;
	}

	public Long getId() {
		return id;
	}

	public Long getDoctorId() {
		return doctorId;
	}

	public LocalDate getBlockDate() {
		return blockDate;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public String getReason() {
		return reason;
	}

	public boolean isActive() {
		return active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void deactivate() {
		this.active = false;
	}

	public void update(LocalDate blockDate, LocalTime startTime, LocalTime endTime, String reason) {
		this.blockDate = blockDate;
		this.startTime = startTime;
		this.endTime = endTime;
		this.reason = reason;
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
