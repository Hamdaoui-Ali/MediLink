package com.medilink.medilink_backend.availability.web;

import java.time.LocalTime;

public record SlotResponse(
		LocalTime startTime,
		LocalTime endTime
) {}
