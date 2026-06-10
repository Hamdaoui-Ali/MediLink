package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.web.AuthenticatedUserResponse;
import com.medilink.medilink_backend.identity.web.LoginRequest;
import com.medilink.medilink_backend.identity.web.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationServiceTest {

	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final JwtTokenService jwtTokenService = mock(JwtTokenService.class);
	private final AuthenticationService authenticationService = new AuthenticationService(
			userRepository,
			passwordEncoder,
			jwtTokenService
	);

	@Test
	void loginReturnsTokenAndUserWhenCredentialsAreValid() {
		User user = new User(new Role(RoleName.PATIENT, "Patient"), "Jane Patient", "jane@example.com", "hash", null);
		Instant expiresAt = Instant.parse("2026-06-11T22:00:00Z");
		when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Patient@123", "hash")).thenReturn(true);
		when(jwtTokenService.generateAccessToken(user)).thenReturn(new JwtTokenService.GeneratedToken(
				"jwt-token",
				"Bearer",
				expiresAt
		));

		LoginResponse response = authenticationService.login(new LoginRequest(" JANE@example.com ", "Patient@123"));

		assertEquals("jwt-token", response.accessToken());
		assertEquals("Bearer", response.tokenType());
		assertEquals(expiresAt, response.expiresAt());
		assertEquals("jane@example.com", response.user().email());
		assertEquals(RoleName.PATIENT, response.user().role());
	}

	@Test
	void loginRejectsUnknownEmailWithSafeError() {
		when(userRepository.findByEmailIgnoreCase("missing@example.com")).thenReturn(Optional.empty());

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.login(new LoginRequest("missing@example.com", "Patient@123"))
		);
	}

	@Test
	void loginRejectsInvalidPasswordWithSafeError() {
		User user = new User(new Role(RoleName.PATIENT, "Patient"), "Jane Patient", "jane@example.com", "hash", null);
		when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("wrong-password", "hash")).thenReturn(false);

		assertThrows(
				InvalidCredentialsException.class,
				() -> authenticationService.login(new LoginRequest("jane@example.com", "wrong-password"))
		);
	}

	@Test
	void loginRejectsInactiveAccount() {
		User user = new User(new Role(RoleName.PATIENT, "Patient"), "Jane Patient", "jane@example.com", "hash", null);
		user.deactivate();
		when(userRepository.findByEmailIgnoreCase("jane@example.com")).thenReturn(Optional.of(user));
		when(passwordEncoder.matches("Patient@123", "hash")).thenReturn(true);

		assertThrows(
				InactiveAccountException.class,
				() -> authenticationService.login(new LoginRequest("jane@example.com", "Patient@123"))
		);
	}

	@Test
	void currentUserReturnsUserResolvedFromAuthenticatedEmail() {
		User user = new User(new Role(RoleName.ADMIN, "Admin"), "Local Admin", "admin@medilink.local", "hash", null);
		when(userRepository.findByEmailIgnoreCase("admin@medilink.local")).thenReturn(Optional.of(user));

		AuthenticatedUserResponse response = authenticationService.currentUser(" ADMIN@medilink.local ");

		assertEquals("Local Admin", response.fullName());
		assertEquals("admin@medilink.local", response.email());
		assertEquals(RoleName.ADMIN, response.role());
		assertEquals(AccountStatus.ACTIVE, response.accountStatus());
		verify(userRepository).findByEmailIgnoreCase("admin@medilink.local");
	}
}
