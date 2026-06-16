package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.RoleName;

public record CreateUserCommand(
		RoleName roleName,
		String fullName,
		String email,
		String passwordHash,
		String phoneNumber
) {
}
