package com.medilink.medilink_backend.administration.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtyRequest(
		@NotBlank(message = "Specialty name is required")
		@Size(max = 120, message = "Specialty name must be 120 characters or fewer")
		String name,

		@Size(max = 500, message = "Description must be 500 characters or fewer")
		String description
) {
}
