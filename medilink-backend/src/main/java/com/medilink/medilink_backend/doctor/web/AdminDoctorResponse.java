package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.domain.DoctorStatus;

public record AdminDoctorResponse(
		Long id,
		Long userId,
		String fullName,
		String email,
		String phoneNumber,
		Long specialtyId,
		String specialtyName,
		String biography,
		Integer consultationDurationMinutes,
		String clinicAddress,
		DoctorStatus status
) {
}
