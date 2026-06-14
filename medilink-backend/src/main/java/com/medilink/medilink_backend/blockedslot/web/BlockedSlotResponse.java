package com.medilink.medilink_backend.blockedslot.web;

import java.time.LocalDate;
import java.time.LocalTime;

public record BlockedSlotResponse(
		Long id,
		Long doctorId,
		LocalDate blockDate,
		LocalTime startTime,
		LocalTime endTime,
		String reason
) {}
