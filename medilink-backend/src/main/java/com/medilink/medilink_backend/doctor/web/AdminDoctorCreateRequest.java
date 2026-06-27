package com.medilink.medilink_backend.doctor.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminDoctorCreateRequest(
		@NotBlank @Size(max = 160) String fullName,
		@NotBlank @Email @Size(max = 190) String email,
		@NotBlank @Size(min = 8, max = 100) String password,
		@Size(max = 40) String phoneNumber,
		@NotNull Long specialtyId,
		@Size(max = 2000) String biography,
		@NotNull @Min(5) Integer consultationDurationMinutes,
		@Size(max = 500) String clinicAddress
) {}
