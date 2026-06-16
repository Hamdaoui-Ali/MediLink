package com.medilink.medilink_backend.identity.web;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.RoleName;

public record AuthenticatedUserResponse(
		Long id,
		String fullName,
		String email,
		RoleName role,
		AccountStatus accountStatus
) {
}
