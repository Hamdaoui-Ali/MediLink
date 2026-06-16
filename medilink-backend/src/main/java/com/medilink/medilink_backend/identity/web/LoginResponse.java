package com.medilink.medilink_backend.identity.web;

import java.time.Instant;

public record LoginResponse(
		String accessToken,
		String tokenType,
		Instant expiresAt,
		AuthenticatedUserResponse user
) {
}
