package com.medilink.medilink_backend.administration.web;

import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;

public record SpecialtyResponse(
		Long id,
		String name,
		String description,
		SpecialtyStatus status
) {
}
