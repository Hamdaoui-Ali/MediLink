package com.medilink.medilink_backend.administration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "specialties")
public class Specialty {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 120)
	private String name;

	@Column(length = 500)
	private String description;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private SpecialtyStatus status;

	protected Specialty() {
	}

	public Specialty(String name, String description) {
		this.name = name;
		this.description = description;
		this.status = SpecialtyStatus.ACTIVE;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public SpecialtyStatus getStatus() {
		return status;
	}

	public void update(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public void activate() {
		this.status = SpecialtyStatus.ACTIVE;
	}

	public void deactivate() {
		this.status = SpecialtyStatus.INACTIVE;
	}
}
