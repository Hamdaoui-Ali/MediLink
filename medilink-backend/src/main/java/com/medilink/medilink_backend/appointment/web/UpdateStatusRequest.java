package com.medilink.medilink_backend.appointment.web;

import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
		@NotNull
		AppointmentStatus status
) {}
