package com.medilink.medilink_backend.doctor.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminDoctorRequest(
		@NotBlank(message = "Full name is required")
		@Size(max = 160, message = "Full name must be 160 characters or fewer")
		String fullName,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 190, message = "Email must be 190 characters or fewer")
		String email,

		@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
		String password,

		@Size(max = 40, message = "Phone number must be 40 characters or fewer")
		String phoneNumber,

		@NotNull(message = "Specialty is required")
		Long specialtyId,

		@Size(max = 5000, message = "Biography must be 5000 characters or fewer")
		String biography,

		@NotNull(message = "Consultation duration is required")
		@Min(value = 5, message = "Consultation duration must be at least 5 minutes")
		@Max(value = 240, message = "Consultation duration must be at most 240 minutes")
		Integer consultationDurationMinutes,

		@Size(max = 500, message = "Clinic address must be 500 characters or fewer")
		String clinicAddress
) {
}
