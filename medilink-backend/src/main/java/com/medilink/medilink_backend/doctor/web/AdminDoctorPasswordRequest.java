package com.medilink.medilink_backend.doctor.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminDoctorPasswordRequest(
		@NotBlank @Size(min = 8, max = 100) String password
) {}
