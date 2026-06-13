package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorAvailabilityService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorAvailabilityControllerTest {

	private final DoctorAvailabilityService availabilityService = mock(DoctorAvailabilityService.class);
	private final DoctorAvailabilityController controller = new DoctorAvailabilityController(availabilityService);

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
	void listAvailabilityReturnsSlotsForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		DoctorAvailabilityService mockService = mock(DoctorAvailabilityService.class);
		when(mockService.resolveDoctor(5L)).thenReturn(mock(com.medilink.medilink_backend.doctor.domain.Doctor.class));
		when(mockService.resolveDoctor(5L).getId()).thenReturn(1L);

		List<DoctorAvailabilityResponse> slots = List.of(
				new DoctorAvailabilityResponse(10L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), true)
		);
		when(mockService.listAvailability(1L)).thenReturn(slots);

		DoctorAvailabilityController controller = new DoctorAvailabilityController(mockService);
		ApiResponse<List<DoctorAvailabilityResponse>> response = controller.listAvailability(jwt);

		assertTrue(response.success());
		assertEquals(1, response.data().size());
	}

	@Test
	void addAvailabilityCreatesSlotAndReturns201() {
		JwtAuthenticationToken jwt = createJwt(5L);
		DoctorAvailabilityService mockService = mock(DoctorAvailabilityService.class);
		com.medilink.medilink_backend.doctor.domain.Doctor doctor = mock(com.medilink.medilink_backend.doctor.domain.Doctor.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		DoctorAvailabilityResponse created = new DoctorAvailabilityResponse(10L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), true);
		when(mockService.addAvailability(1L, new DoctorAvailabilityRequest(1, "09:00", "17:00")))
				.thenReturn(created);

		DoctorAvailabilityController controller = new DoctorAvailabilityController(mockService);
		ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> response = controller.addAvailability(jwt,
				new DoctorAvailabilityRequest(1, "09:00", "17:00"));

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().success());
	}

	@Test
	void deleteDeactivatesSlot() {
		JwtAuthenticationToken jwt = createJwt(5L);
		DoctorAvailabilityService mockService = mock(DoctorAvailabilityService.class);
		com.medilink.medilink_backend.doctor.domain.Doctor doctor = mock(com.medilink.medilink_backend.doctor.domain.Doctor.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		DoctorAvailabilityResponse deactivated = new DoctorAvailabilityResponse(10L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0), false);
		when(mockService.deactivateAvailability(1L, 10L)).thenReturn(deactivated);

		DoctorAvailabilityController controller = new DoctorAvailabilityController(mockService);
		ApiResponse<DoctorAvailabilityResponse> response = controller.deactivateAvailability(jwt, 10L);

		assertTrue(response.success());
		assertEquals(10L, response.data().id());
	}

	@Test
	void allEndpointsRequireDoctorRole() throws NoSuchMethodException {
		Class<DoctorAvailabilityController> clazz = DoctorAvailabilityController.class;
		PreAuthorize classAnnotation = clazz.getAnnotation(PreAuthorize.class);

		assertNotNull(classAnnotation);
		assertEquals("hasRole('DOCTOR')", classAnnotation.value());
	}
}
