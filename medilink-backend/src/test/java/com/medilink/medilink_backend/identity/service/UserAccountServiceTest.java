package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.RoleRepository;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccountServiceTest {

	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserAccountService userAccountService = new UserAccountService(roleRepository, userRepository);

	@Test
	void createUserNormalizesEmailAndPersistsActiveUserWithRole() {
		Role patientRole = new Role(RoleName.PATIENT, "Patient account");
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

		User user = userAccountService.createUser(new CreateUserCommand(
				RoleName.PATIENT,
				"  Jane Patient  ",
				"  JANE@Example.COM ",
				"hashed-password",
				"  +15551234567 "
		));

		assertEquals("Jane Patient", user.getFullName());
		assertEquals("jane@example.com", user.getEmail());
		assertEquals("hashed-password", user.getPasswordHash());
		assertEquals("+15551234567", user.getPhoneNumber());
		assertEquals(RoleName.PATIENT, user.getRole().getName());
		assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
	}

	@Test
	void createUserRejectsDuplicateEmailBeforeRoleLookup() {
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

		assertThrows(EmailAlreadyUsedException.class, () -> userAccountService.createUser(new CreateUserCommand(
				RoleName.PATIENT,
				"Jane Patient",
				"jane@example.com",
				"hashed-password",
				null
		)));

		verify(roleRepository, never()).findByName(RoleName.PATIENT);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void createUserFailsWhenRoleSeedDataIsMissing() {
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.empty());

		assertThrows(RoleNotFoundException.class, () -> userAccountService.createUser(new CreateUserCommand(
				RoleName.PATIENT,
				"Jane Patient",
				"jane@example.com",
				"hashed-password",
				null
		)));
	}

	@Test
	void getByEmailNormalizesEmailBeforeLookup() {
		User user = new User(new Role(RoleName.ADMIN, "Admin"), "Local Admin", "admin@medilink.local", "hash", null);
		when(userRepository.findByEmailIgnoreCase("admin@medilink.local")).thenReturn(Optional.of(user));

		User foundUser = userAccountService.getByEmail("  ADMIN@MediLink.Local ");

		assertEquals("admin@medilink.local", foundUser.getEmail());
	}

	@Test
	void getByEmailThrowsWhenUserDoesNotExist() {
		when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThrows(UserNotFoundException.class, () -> userAccountService.getByEmail("missing@example.com"));
	}

	@Test
	void activateDeactivateAndDisableUpdateAccountStatus() {
		User user = new User(new Role(RoleName.PATIENT, "Patient"), "Jane Patient", "jane@example.com", "hash", null);
		when(userRepository.findById(1L)).thenReturn(Optional.of(user));

		assertEquals(AccountStatus.INACTIVE, userAccountService.deactivate(1L).getAccountStatus());
		assertEquals(AccountStatus.ACTIVE, userAccountService.activate(1L).getAccountStatus());
		assertEquals(AccountStatus.DISABLED, userAccountService.disable(1L).getAccountStatus());
	}

	@Test
	void trimToNullTrimsValueAndConvertsBlankToNull() {
		assertEquals("+15551234567", userAccountService.trimToNull("  +15551234567 "));
		assertNull(userAccountService.trimToNull(""));
		assertNull(userAccountService.trimToNull(null));
	}
}
