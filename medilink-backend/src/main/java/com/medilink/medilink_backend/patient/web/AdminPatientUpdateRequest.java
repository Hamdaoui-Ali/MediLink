package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AdminPatientUpdateRequest(
		@NotBlank @Size(max = 160) String fullName,
		@NotBlank @Email @Size(max = 190) String email,
		@Size(max = 40) String phoneNumber,
		@Past LocalDate dateOfBirth,
		@NotNull Gender gender,
		@Size(max = 500) String address
) {}
