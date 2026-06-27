package com.medilink.medilink_backend.appointment.web;

import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.service.AppointmentService;
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
import static org.mockito.Mockito.when;

class DoctorAppointmentControllerTest {

	private final AppointmentService appointmentService = mock(AppointmentService.class);

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
	void listAppointmentsReturnsAppointmentsForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		List<AppointmentResponse> appointments = List.of(response(AppointmentStatus.CONFIRMED, null));
		when(mockService.listFilteredAppointments(1L, null, null, null)).thenReturn(appointments);

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<List<AppointmentResponse>> response = controller.listAppointments(jwt, null, null, null);

		assertTrue(response.success());
		assertEquals(1, response.data().size());
		assertEquals(AppointmentStatus.CONFIRMED, response.data().getFirst().status());
	}

	@Test
	void listAppointmentsSupportsDateRangeFilter() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		LocalDate from = LocalDate.of(2026, 6, 1);
		LocalDate to = LocalDate.of(2026, 6, 30);
		List<AppointmentResponse> appointments = List.of(response(AppointmentStatus.CONFIRMED, null));
		when(mockService.listFilteredAppointments(1L, null, from, to)).thenReturn(appointments);

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<List<AppointmentResponse>> response = controller.listAppointments(jwt, null, from, to);

		assertTrue(response.success());
		assertEquals(1, response.data().size());
	}

	@Test
	void listAppointmentsSupportsStatusAndDateRangeFilter() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		LocalDate from = LocalDate.of(2026, 6, 1);
		LocalDate to = LocalDate.of(2026, 6, 30);
		when(mockService.listFilteredAppointments(1L, AppointmentStatus.COMPLETED, from, to))
				.thenReturn(List.of());

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<List<AppointmentResponse>> response = controller.listAppointments(jwt, AppointmentStatus.COMPLETED, from, to);

		assertTrue(response.success());
		assertEquals(0, response.data().size());
	}

	@Test
	void updateNotesSavesNotesOnOwnAppointment() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		AppointmentResponse response = response(AppointmentStatus.CONFIRMED, "Patient doing well");
		when(mockService.updateNotes(1L, 10L, "Patient doing well")).thenReturn(response);

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<AppointmentResponse> result = controller.updateNotes(jwt, 10L, new UpdateNotesRequest("Patient doing well"));

		assertTrue(result.success());
		assertEquals("Patient doing well", result.data().doctorNotes());
	}

	@Test
	void updateStatusUpdatesAppointmentStatus() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);

		AppointmentResponse response = response(AppointmentStatus.COMPLETED, null);
		when(mockService.updateStatus(1L, 10L, AppointmentStatus.COMPLETED)).thenReturn(response);

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<AppointmentResponse> result = controller.updateStatus(jwt, 10L,
				new UpdateStatusRequest(AppointmentStatus.COMPLETED));

		assertTrue(result.success());
		assertEquals(AppointmentStatus.COMPLETED, result.data().status());
	}

	@Test
	void allEndpointsRequireDoctorRole() {
		PreAuthorize classAnnotation = DoctorAppointmentController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(classAnnotation);
		assertEquals("hasRole('DOCTOR')", classAnnotation.value());
	}

	@Test
	void listPatientHistoryReturnsHistoryForAuthenticatedDoctor() {
		JwtAuthenticationToken jwt = createJwt(5L);
		AppointmentService mockService = mock(AppointmentService.class);
		com.medilink.medilink_backend.appointment.domain.DoctorRef doctor = mock(com.medilink.medilink_backend.appointment.domain.DoctorRef.class);
		when(mockService.resolveDoctor(5L)).thenReturn(doctor);
		when(doctor.getId()).thenReturn(1L);
		when(mockService.listPatientHistoryForDoctor(1L, 2L))
				.thenReturn(List.of(response(AppointmentStatus.COMPLETED, "Follow-up complete")));

		DoctorAppointmentController controller = new DoctorAppointmentController(mockService);
		ApiResponse<List<AppointmentResponse>> result = controller.listPatientHistory(jwt, 2L);

		assertTrue(result.success());
		assertEquals(1, result.data().size());
		assertEquals("Jane Patient", result.data().getFirst().patientName());
	}

	private AppointmentResponse response(AppointmentStatus status, String doctorNotes) {
		return new AppointmentResponse(
				10L,
				1L,
				2L,
				"Jane Patient",
				"jane.patient@medilink.local",
				"+15551234567",
				LocalDate.of(1990, 1, 15),
				"FEMALE",
				"123 Patient St",
				LocalDate.of(2026, 6, 15),
				LocalTime.of(10, 0),
				LocalTime.of(10, 30),
				status,
				"Checkup",
				doctorNotes
		);
	}
}
