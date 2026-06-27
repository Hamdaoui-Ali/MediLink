package com.medilink.medilink_backend.doctor.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record DoctorAvailabilityRequest(
		@Min(1) @Max(7)
		int dayOfWeek,

		@NotNull
		String startTime,

		@NotNull
		String endTime
) {}
