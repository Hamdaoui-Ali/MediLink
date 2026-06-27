package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.domain.Gender;

import java.time.LocalDate;

public record AdminPatientResponse(
		Long id,
		Long userId,
		String fullName,
		String email,
		String phoneNumber,
		String accountStatus,
		LocalDate dateOfBirth,
		Gender gender,
		String address
) {}
