package com.medilink.medilink_backend.doctor.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record DoctorProfileUpdateRequest(
		@Size(max = 2000) String biography,
		@Size(max = 500) String clinicAddress,
		String phoneNumber,
		@Min(5) Integer consultationDurationMinutes
) {}
