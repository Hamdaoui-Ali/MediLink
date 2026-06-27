package com.medilink.medilink_backend.appointment.domain;

import com.medilink.medilink_backend.patient.domain.Gender;

import java.time.LocalDate;

public record PatientRef(
		Long id,
		String fullName,
		String email,
		String phoneNumber,
		LocalDate dateOfBirth,
		Gender gender,
		String address
) {}
