package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.RoleName;

public class RoleNotFoundException extends RuntimeException {

	public RoleNotFoundException(RoleName roleName) {
		super("Role was not found: " + roleName);
	}
}
