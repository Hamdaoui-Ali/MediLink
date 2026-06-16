package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.domain.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRegistrationRequest(
		@NotBlank(message = "Full name is required")
		@Size(max = 160, message = "Full name must be 160 characters or fewer")
		String fullName,

		@NotBlank(message = "Email is required")
		@Email(message = "Email must be valid")
		@Size(max = 190, message = "Email must be 190 characters or fewer")
		String email,

		@NotBlank(message = "Password is required")
		@Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters")
		String password,

		@NotBlank(message = "Phone number is required")
		@Size(max = 40, message = "Phone number must be 40 characters or fewer")
		String phoneNumber,

		@Past(message = "Date of birth must be in the past")
		LocalDate dateOfBirth,

		Gender gender,

		@Size(max = 500, message = "Address must be 500 characters or fewer")
		String address
) {
}
