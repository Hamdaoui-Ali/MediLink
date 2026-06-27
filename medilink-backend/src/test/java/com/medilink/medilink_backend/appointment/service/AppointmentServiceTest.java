package com.medilink.medilink_backend.appointment.service;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.domain.PatientRef;
import com.medilink.medilink_backend.appointment.domain.PatientRefEntity;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.repository.PatientRefRepository;
import com.medilink.medilink_backend.appointment.web.AppointmentResponse;
import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentServiceTest {

	private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
	private final DoctorRefRepository doctorRefRepository = mock(DoctorRefRepository.class);
	private final PatientRefRepository patientRefRepository = mock(PatientRefRepository.class);
	private final PatientRepository patientRepository = mock(PatientRepository.class);
	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final AppointmentService service = new AppointmentService(
			appointmentRepository, doctorRefRepository, patientRefRepository,
			patientRepository, notificationRepository);

	@Test
	void resolveDoctorReturnsActiveDoctor() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctor.getId()).thenReturn(1L);
		when(doctorRefRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorRef resolved = service.resolveDoctor(5L);

		assertEquals(1L, resolved.getId());
	}

	@Test
	void resolveDoctorThrowsWhenNotFound() {
		when(doctorRefRepository.findByUserId(99L)).thenReturn(Optional.empty());

		assertThrows(DoctorRefNotFoundException.class, () -> service.resolveDoctor(99L));
	}

	@Test
	void resolveDoctorThrowsWhenInactive() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(false);
		when(doctorRefRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		assertThrows(DoctorRefNotFoundException.class, () -> service.resolveDoctor(5L));
	}

	@Test
	void getAppointmentReturnsOwnAppointment() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getId()).thenReturn(10L);
		when(appointment.getDoctorId()).thenReturn(1L);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.getAppointment(1L, 10L);

		assertEquals(10L, response.id());
		assertEquals(AppointmentStatus.CONFIRMED, response.status());
	}

	@Test
	void getAppointmentThrowsWhenNotOwnedByDoctor() {
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.empty());

		assertThrows(AppointmentNotFoundException.class, () -> service.getAppointment(1L, 10L));
	}

	@Test
	void updateNotesSavesDoctorNotes() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getDoctorNotes()).thenReturn("Patient responded well");
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.updateNotes(1L, 10L, "Patient responded well");

		assertEquals("Patient responded well", response.doctorNotes());
	}

	@Test
	void updateStatusAllowsConfirmedToCompleted() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus())
				.thenReturn(AppointmentStatus.CONFIRMED, AppointmentStatus.COMPLETED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.updateStatus(1L, 10L, AppointmentStatus.COMPLETED);

		assertEquals(AppointmentStatus.COMPLETED, response.status());
	}

	@Test
	void updateStatusRejectsInvalidTransition() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		assertThrows(InvalidAppointmentStatusException.class,
				() -> service.updateStatus(1L, 10L, AppointmentStatus.CONFIRMED));
	}

	@Test
	void updateStatusRejectsTransitionFromTerminalState() {
		Appointment completed = mock(Appointment.class);
		when(completed.getStatus()).thenReturn(AppointmentStatus.COMPLETED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(completed));

		assertThrows(InvalidAppointmentStatusException.class,
				() -> service.updateStatus(1L, 10L, AppointmentStatus.CONFIRMED));
	}

	@Test
	void updateStatusAllowsConfirmedToCancelled() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus())
				.thenReturn(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.updateStatus(1L, 10L, AppointmentStatus.CANCELLED);

		assertEquals(AppointmentStatus.CANCELLED, response.status());
	}

	@Test
	void updateStatusAllowsConfirmedToMissed() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus())
				.thenReturn(AppointmentStatus.CONFIRMED, AppointmentStatus.MISSED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.updateStatus(1L, 10L, AppointmentStatus.MISSED);

		assertEquals(AppointmentStatus.MISSED, response.status());
	}

	@Test
	void updateStatusAllowsRescheduledToConfirmed() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus())
				.thenReturn(AppointmentStatus.RESCHEDULED, AppointmentStatus.CONFIRMED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		AppointmentResponse response = service.updateStatus(1L, 10L, AppointmentStatus.CONFIRMED);

		assertEquals(AppointmentStatus.CONFIRMED, response.status());
	}

	@Test
	void listAppointmentsReturnsAppointmentsForDoctor() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
		when(appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(1L))
				.thenReturn(List.of(appointment));
		when(patientRepository.findPatientRefsByIds(List.of(2L)))
				.thenReturn(List.of(patientRef()));

		List<AppointmentResponse> appointments = service.listAppointments(1L);

		assertEquals(1, appointments.size());
		assertEquals(AppointmentStatus.CONFIRMED, appointments.getFirst().status());
		assertEquals("Jane Patient", appointments.getFirst().patientName());
	}

	@Test
	void listFilteredAppointmentsSupportsDateRangeOnly() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
		LocalDate from = LocalDate.of(2026, 6, 1);
		LocalDate to = LocalDate.of(2026, 6, 30);
		when(appointmentRepository.findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(1L, from, to))
				.thenReturn(List.of(appointment));
		when(patientRepository.findPatientRefsByIds(List.of(2L)))
				.thenReturn(List.of(patientRef()));

		List<AppointmentResponse> appointments = service.listFilteredAppointments(1L, null, from, to);

		assertEquals(1, appointments.size());
		assertEquals("Jane Patient", appointments.getFirst().patientName());
	}

	@Test
	void listFilteredAppointmentsSupportsStatusAndDateRange() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.COMPLETED);
		LocalDate from = LocalDate.of(2026, 6, 1);
		LocalDate to = LocalDate.of(2026, 6, 30);
		when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
				1L, AppointmentStatus.COMPLETED, from, to))
				.thenReturn(List.of(appointment));
		when(patientRepository.findPatientRefsByIds(List.of(2L)))
				.thenReturn(List.of(patientRef()));

		List<AppointmentResponse> appointments = service.listFilteredAppointments(
				1L, AppointmentStatus.COMPLETED, from, to);

		assertEquals(1, appointments.size());
		assertEquals(AppointmentStatus.COMPLETED, appointments.getFirst().status());
	}

	@Test
	void listFilteredAppointmentsReturnsEmptyListWhenNoResults() {
		LocalDate from = LocalDate.of(2026, 1, 1);
		LocalDate to = LocalDate.of(2026, 1, 31);
		when(appointmentRepository.findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
				1L, AppointmentStatus.MISSED, from, to))
				.thenReturn(List.of());

		List<AppointmentResponse> appointments = service.listFilteredAppointments(
				1L, AppointmentStatus.MISSED, from, to);

		assertTrue(appointments.isEmpty());
	}

	@Test
	void listFilteredAppointmentsNoFiltersDelegatesToListAll() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(1L))
				.thenReturn(List.of(appointment));
		when(patientRepository.findPatientRefsByIds(List.of(2L)))
				.thenReturn(List.of(patientRef()));

		service.listFilteredAppointments(1L, null, null, null);

		verify(appointmentRepository).findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(1L);
	}

	@Test
	void terminalStatesBlockFurtherTransitions() {
		for (AppointmentStatus terminal : List.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED, AppointmentStatus.MISSED)) {
			assertTrue(terminal.isTerminal());
		}
	}

	@Test
	void updateNotesThrowsWhenAppointmentNotFound() {
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.empty());

		assertThrows(AppointmentNotFoundException.class,
				() -> service.updateNotes(1L, 10L, "Some notes"));
	}

	@Test
	void createAppointmentAlsoCreatesConfirmationNotification() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctorRefRepository.findById(5L)).thenReturn(Optional.of(doctor));
		when(doctorRefRepository.findConsultationDurationById(5L)).thenReturn(Optional.of(30));
		when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
				any(Long.class), any(LocalDate.class), any(List.class)))
				.thenReturn(List.of());

		Appointment saved = mock(Appointment.class);
		when(saved.getId()).thenReturn(100L);
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

		PatientRefEntity patientRef = mock(PatientRefEntity.class);
		when(patientRef.getUserId()).thenReturn(200L);
		when(patientRefRepository.findById(3L)).thenReturn(Optional.of(patientRef));

		AppointmentResponse response = service.createAppointment(
				3L, 5L, LocalDate.now().plusDays(1), LocalTime.of(9, 0), "Checkup");

		assertEquals(100L, response.id());
		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	void bookingSucceedsEvenWhenNotificationCreationFails() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctorRefRepository.findById(5L)).thenReturn(Optional.of(doctor));
		when(doctorRefRepository.findConsultationDurationById(5L)).thenReturn(Optional.of(30));
		when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
				any(Long.class), any(LocalDate.class), any(List.class)))
				.thenReturn(List.of());

		Appointment saved = mock(Appointment.class);
		when(saved.getId()).thenReturn(101L);
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

		when(patientRefRepository.findById(3L)).thenThrow(new RuntimeException("DB error"));

		AppointmentResponse response = service.createAppointment(
				3L, 5L, LocalDate.now().plusDays(1), LocalTime.of(10, 0), "Checkup");

		assertEquals(101L, response.id());
	}

	@Test
	void bookingSucceedsWhenPatientRefNotFoundForNotification() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctorRefRepository.findById(5L)).thenReturn(Optional.of(doctor));
		when(doctorRefRepository.findConsultationDurationById(5L)).thenReturn(Optional.of(30));
		when(appointmentRepository.findByDoctorIdAndAppointmentDateAndStatusIn(
				any(Long.class), any(LocalDate.class), any(List.class)))
				.thenReturn(List.of());

		Appointment saved = mock(Appointment.class);
		when(saved.getId()).thenReturn(102L);
		when(appointmentRepository.save(any(Appointment.class))).thenReturn(saved);

		when(patientRefRepository.findById(3L)).thenReturn(Optional.empty());

		AppointmentResponse response = service.createAppointment(
				3L, 5L, LocalDate.now().plusDays(1), LocalTime.of(11, 0), "Checkup");

		assertEquals(102L, response.id());
	}

	@Test
	void updateStatusRejectsTransitionFromCancelled() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.CANCELLED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		assertThrows(InvalidAppointmentStatusException.class,
				() -> service.updateStatus(1L, 10L, AppointmentStatus.CONFIRMED));
	}

	@Test
	void updateStatusRejectsTransitionFromMissed() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.MISSED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		assertThrows(InvalidAppointmentStatusException.class,
				() -> service.updateStatus(1L, 10L, AppointmentStatus.CONFIRMED));
	}

	@Test
	void updateStatusRejectsRescheduledToRescheduled() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.RESCHEDULED);
		when(appointmentRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(appointment));

		assertThrows(InvalidAppointmentStatusException.class,
				() -> service.updateStatus(1L, 10L, AppointmentStatus.RESCHEDULED));
	}

	@Test
	void listPatientHistoryForDoctorReturnsOnlyDoctorPatientAppointments() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointment.getStatus()).thenReturn(AppointmentStatus.COMPLETED);
		when(appointmentRepository.findByDoctorIdAndPatientIdOrderByAppointmentDateDescStartTimeDesc(1L, 2L))
				.thenReturn(List.of(appointment));
		when(patientRepository.findPatientRefsByIds(List.of(2L)))
				.thenReturn(List.of(patientRef()));

		List<AppointmentResponse> history = service.listPatientHistoryForDoctor(1L, 2L);

		assertEquals(1, history.size());
		assertEquals("Jane Patient", history.getFirst().patientName());
		assertEquals("jane.patient@medilink.local", history.getFirst().patientEmail());
	}

	private PatientRef patientRef() {
		return new PatientRef(
				2L,
				"Jane Patient",
				"jane.patient@medilink.local",
				"+15551234567",
				LocalDate.of(1990, 1, 15),
				null,
				"123 Patient St"
		);
	}
}
