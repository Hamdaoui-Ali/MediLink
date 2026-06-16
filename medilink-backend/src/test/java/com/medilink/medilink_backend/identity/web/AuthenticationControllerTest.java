package com.medilink.medilink_backend.identity.web;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.service.AuthenticationService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthenticationControllerTest {

	@Test
	void loginReturnsSuccessfulApiResponse() {
		AuthenticationService authenticationService = mock(AuthenticationService.class);
		AuthenticationController controller = new AuthenticationController(authenticationService);
		LoginRequest request = new LoginRequest("jane@example.com", "Patient@123");
		LoginResponse loginResponse = new LoginResponse(
				"jwt-token",
				"Bearer",
				Instant.parse("2026-06-11T22:00:00Z"),
				new AuthenticatedUserResponse(1L, "Jane Patient", "jane@example.com", RoleName.PATIENT, AccountStatus.ACTIVE)
		);
		when(authenticationService.login(request)).thenReturn(loginResponse);

		ApiResponse<LoginResponse> response = controller.login(request);

		assertTrue(response.success());
		assertEquals(loginResponse, response.data());
	}

	@Test
	void meReturnsCurrentAuthenticatedUser() {
		AuthenticationService authenticationService = mock(AuthenticationService.class);
		AuthenticationController controller = new AuthenticationController(authenticationService);
		AuthenticatedUserResponse user = new AuthenticatedUserResponse(
				1L,
				"Jane Patient",
				"jane@example.com",
				RoleName.PATIENT,
				AccountStatus.ACTIVE
		);
		when(authenticationService.currentUser("jane@example.com")).thenReturn(user);

		ApiResponse<AuthenticatedUserResponse> response = controller.me(
				new TestingAuthenticationToken("jane@example.com", null)
		);

		assertTrue(response.success());
		assertEquals(user, response.data());
	}
}
