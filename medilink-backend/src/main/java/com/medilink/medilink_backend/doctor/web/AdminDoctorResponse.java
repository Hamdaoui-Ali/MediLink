package com.medilink.medilink_backend.doctor.web;

public record AdminDoctorResponse(
		Long id,
		Long userId,
		String fullName,
		String email,
		String phoneNumber,
		String accountStatus,
		Long specialtyId,
		String specialtyName,
		String biography,
		Integer consultationDurationMinutes,
		String clinicAddress,
		String status
) {}
