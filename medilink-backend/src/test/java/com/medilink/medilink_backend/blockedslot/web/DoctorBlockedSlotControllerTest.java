package com.medilink.medilink_backend.blockedslot.web;

import com.medilink.medilink_backend.blockedslot.service.BlockedSlotService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorBlockedSlotControllerTest {

	private final BlockedSlotService blockedSlotService = mock(BlockedSlotService.class);

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
	void listBlockedSlotsReturnsSlotsForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		BlockedSlotService mockService = mock(BlockedSlotService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		List<BlockedSlotResponse> slots = List.of(
				new BlockedSlotResponse(1L, 1L, LocalDate.of(2026, 6, 20),
						LocalTime.of(14, 0), LocalTime.of(16, 0), "Vacation")
		);
		when(mockService.listBlockedSlots(1L)).thenReturn(slots);

		DoctorBlockedSlotController controller = new DoctorBlockedSlotController(mockService);
		ApiResponse<List<BlockedSlotResponse>> response = controller.listBlockedSlots(jwt);

		assertTrue(response.success());
		assertEquals(1, response.data().size());
	}

	@Test
	void createBlockedSlotCreatesSlotForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		BlockedSlotService mockService = mock(BlockedSlotService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.of(2026, 6, 20), LocalTime.of(14, 0), LocalTime.of(16, 0), "Vacation"
		);
		BlockedSlotResponse expected = new BlockedSlotResponse(1L, 1L, LocalDate.of(2026, 6, 20),
				LocalTime.of(14, 0), LocalTime.of(16, 0), "Vacation");
		when(mockService.createBlockedSlot(1L, request)).thenReturn(expected);

		DoctorBlockedSlotController controller = new DoctorBlockedSlotController(mockService);
		ApiResponse<BlockedSlotResponse> response = controller.createBlockedSlot(jwt, request);

		assertTrue(response.success());
		assertEquals("Vacation", response.data().reason());
	}

	@Test
	void deleteBlockedSlotRemovesSlotForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		BlockedSlotService mockService = mock(BlockedSlotService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		DoctorBlockedSlotController controller = new DoctorBlockedSlotController(mockService);
		ApiResponse<Void> response = controller.deleteBlockedSlot(jwt, 10L);

		assertTrue(response.success());
		verify(mockService).deleteBlockedSlot(1L, 10L);
	}

	@Test
	void allEndpointsRequireDoctorRole() {
		PreAuthorize classAnnotation = DoctorBlockedSlotController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(classAnnotation);
		assertEquals("hasRole('DOCTOR')", classAnnotation.value());
	}
}
