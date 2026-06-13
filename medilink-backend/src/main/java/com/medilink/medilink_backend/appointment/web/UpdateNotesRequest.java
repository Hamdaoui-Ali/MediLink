package com.medilink.medilink_backend.appointment.web;

import jakarta.validation.constraints.NotBlank;

public record UpdateNotesRequest(
		@NotBlank
		String notes
) {}
