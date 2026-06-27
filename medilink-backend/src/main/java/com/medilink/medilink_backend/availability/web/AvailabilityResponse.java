package com.medilink.medilink_backend.availability.web;

import java.time.LocalTime;

public record AvailabilityResponse(
		Long id,
		Long doctorId,
		Integer dayOfWeek,
		LocalTime startTime,
		LocalTime endTime
) {}
