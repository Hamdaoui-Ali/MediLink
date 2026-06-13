package com.medilink.medilink_backend.doctor.web;

import java.time.LocalTime;

public record DoctorAvailabilityResponse(
		Long id,
		int dayOfWeek,
		LocalTime startTime,
		LocalTime endTime,
		boolean isActive
) {}
