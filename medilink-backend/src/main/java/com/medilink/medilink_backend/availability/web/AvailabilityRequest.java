package com.medilink.medilink_backend.availability.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record AvailabilityRequest(
		@NotNull @Min(1) @Max(7) Integer dayOfWeek,
		@NotNull LocalTime startTime,
		@NotNull LocalTime endTime
) {}
