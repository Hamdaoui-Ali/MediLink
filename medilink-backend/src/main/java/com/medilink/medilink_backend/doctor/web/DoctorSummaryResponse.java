package com.medilink.medilink_backend.doctor.web;

public record DoctorSummaryResponse(
		Long id,
		String fullName,
		String specialtyName,
		String biography,
		int consultationDurationMinutes,
		String clinicAddress
) {}
