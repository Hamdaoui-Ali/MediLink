package com.medilink.medilink_backend.patient.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminPatientPasswordRequest(
		@NotBlank @Size(min = 8, max = 100) String password
) {}
