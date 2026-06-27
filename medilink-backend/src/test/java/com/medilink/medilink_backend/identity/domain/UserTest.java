package com.medilink.medilink_backend.identity.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserTest {

	@Test
	void updateProfileChangesEditableIdentityFields() {
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Old", "old@example.com", "hash", null);

		user.updateProfile("Dr New", "new@example.com", "+1555");

		assertEquals("Dr New", user.getFullName());
		assertEquals("new@example.com", user.getEmail());
		assertEquals("+1555", user.getPhoneNumber());
	}

	@Test
	void updatePasswordHashChangesStoredPasswordHash() {
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "old-hash", null);

		user.updatePasswordHash("new-hash");

		assertEquals("new-hash", user.getPasswordHash());
	}
}
