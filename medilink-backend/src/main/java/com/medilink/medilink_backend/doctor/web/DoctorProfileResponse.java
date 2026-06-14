package com.medilink.medilink_backend.doctor.web;

public record DoctorProfileResponse(
		Long id,
		Long userId,
		String fullName,
		String email,
		String phoneNumber,
		String accountStatus,
		String specialtyName,
		String biography,
		int consultationDurationMinutes,
		String clinicAddress,
		String status
) {}
