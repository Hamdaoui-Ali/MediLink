package com.medilink.medilink_backend.blockedslot.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record BlockedSlotRequest(
		@NotNull LocalDate blockDate,
		@NotNull LocalTime startTime,
		@NotNull LocalTime endTime,
		String reason
) {}
