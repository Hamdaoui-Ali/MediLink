package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorProfileService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorProfileControllerTest {

	private final DoctorProfileService doctorProfileService = mock(DoctorProfileService.class);

	private JwtAuthenticationToken createJwt(Long userId) {
		Jwt jwt = Jwt.withTokenValue("test-token")
				.header("alg", "HS256")
				.claim("userId", userId)
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(3600))
				.build();
		return new JwtAuthenticationToken(jwt);
	}

	@Test
	void getProfileReturnsProfileForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		DoctorProfileService mockService = mock(DoctorProfileService.class);
		DoctorProfileResponse expected = new DoctorProfileResponse(
				1L, 5L, "Dr. Test", "dr.test@medilink.local", "+15551234567",
				"ACTIVE", "Cardiology", "Bio", 30, "123 Clinic", "ACTIVE"
		);
		when(mockService.getProfile(5L)).thenReturn(expected);

		DoctorProfileController controller = new DoctorProfileController(mockService);
		ApiResponse<DoctorProfileResponse> response = controller.getProfile(jwt);

		assertTrue(response.success());
		assertEquals("Dr. Test", response.data().fullName());
	}

	@Test
	void updateProfileUpdatesForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		DoctorProfileService mockService = mock(DoctorProfileService.class);
		DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest(
				"New bio", "New address", "+15559999999", 45
		);
		DoctorProfileResponse expected = new DoctorProfileResponse(
				1L, 5L, "Dr. Test", "dr.test@medilink.local", "+15559999999",
				"ACTIVE", "Cardiology", "New bio", 45, "New address", "ACTIVE"
		);
		when(mockService.updateProfile(5L, request)).thenReturn(expected);

		DoctorProfileController controller = new DoctorProfileController(mockService);
		ApiResponse<DoctorProfileResponse> response = controller.updateProfile(jwt, request);

		assertTrue(response.success());
		assertEquals("New bio", response.data().biography());
	}

	@Test
	void allEndpointsRequireDoctorRole() {
		PreAuthorize classAnnotation = DoctorProfileController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(classAnnotation);
		assertEquals("hasRole('DOCTOR')", classAnnotation.value());
	}
}
