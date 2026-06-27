package com.medilink.medilink_backend.availability.web;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.availability.service.DoctorAvailabilityService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorAvailabilityManagementControllerTest {

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
	void listAvailabilityReturnsAuthenticatedDoctorsRanges() {
		DoctorAvailabilityService service = mock(DoctorAvailabilityService.class);
		DoctorRef doctor = mock(DoctorRef.class);
		when(service.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(10L);
		when(service.listAvailability(10L)).thenReturn(List.of(
				new AvailabilityResponse(1L, 10L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0))
		));

		DoctorAvailabilityManagementController controller = new DoctorAvailabilityManagementController(service);
		ApiResponse<List<AvailabilityResponse>> response = controller.listAvailability(createJwt(5L));

		assertTrue(response.success());
		assertEquals(1, response.data().size());
		assertEquals(10L, response.data().getFirst().doctorId());
	}

	@Test
	void createAvailabilityCreatesRangeForAuthenticatedDoctor() {
		DoctorAvailabilityService service = mock(DoctorAvailabilityService.class);
		DoctorRef doctor = mock(DoctorRef.class);
		AvailabilityRequest request = new AvailabilityRequest(2, LocalTime.of(10, 0), LocalTime.of(14, 0));
		AvailabilityResponse expected = new AvailabilityResponse(2L, 10L, 2, LocalTime.of(10, 0), LocalTime.of(14, 0));
		when(service.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(10L);
		when(service.createAvailability(10L, request)).thenReturn(expected);

		DoctorAvailabilityManagementController controller = new DoctorAvailabilityManagementController(service);
		ApiResponse<AvailabilityResponse> response = controller.createAvailability(createJwt(5L), request);

		assertTrue(response.success());
		assertEquals(2L, response.data().id());
	}

	@Test
	void updateAvailabilityUpdatesRangeForAuthenticatedDoctor() {
		DoctorAvailabilityService service = mock(DoctorAvailabilityService.class);
		DoctorRef doctor = mock(DoctorRef.class);
		AvailabilityRequest request = new AvailabilityRequest(3, LocalTime.of(8, 0), LocalTime.of(11, 0));
		AvailabilityResponse expected = new AvailabilityResponse(3L, 10L, 3, LocalTime.of(8, 0), LocalTime.of(11, 0));
		when(service.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(10L);
		when(service.updateAvailability(10L, 3L, request)).thenReturn(expected);

		DoctorAvailabilityManagementController controller = new DoctorAvailabilityManagementController(service);
		ApiResponse<AvailabilityResponse> response = controller.updateAvailability(createJwt(5L), 3L, request);

		assertTrue(response.success());
		assertEquals(LocalTime.of(8, 0), response.data().startTime());
	}

	@Test
	void deleteAvailabilityDeletesRangeForAuthenticatedDoctor() {
		DoctorAvailabilityService service = mock(DoctorAvailabilityService.class);
		DoctorRef doctor = mock(DoctorRef.class);
		when(service.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(10L);

		DoctorAvailabilityManagementController controller = new DoctorAvailabilityManagementController(service);
		ApiResponse<Void> response = controller.deleteAvailability(createJwt(5L), 3L);

		assertTrue(response.success());
		verify(service).deleteAvailability(10L, 3L);
	}

	@Test
	void allEndpointsRequireDoctorRole() {
		PreAuthorize classAnnotation = DoctorAvailabilityManagementController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(classAnnotation);
		assertEquals("hasRole('DOCTOR')", classAnnotation.value());
	}
}
