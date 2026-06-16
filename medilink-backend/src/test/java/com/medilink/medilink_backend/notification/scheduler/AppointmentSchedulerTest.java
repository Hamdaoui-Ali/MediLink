package com.medilink.medilink_backend.notification.scheduler;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AppointmentSchedulerTest {

	private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
	private final NotificationRepository notificationRepository = mock(NotificationRepository.class);
	private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
	private final AppointmentScheduler scheduler = new AppointmentScheduler(
			appointmentRepository, notificationRepository, jdbcTemplate);

	@Test
	void createsNotificationRecordsForConfirmedAppointments() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getId()).thenReturn(1L);
		when(appointment.getPatientId()).thenReturn(10L);
		when(appointmentRepository.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any()))
				.thenReturn(List.of(appointment));
		when(notificationRepository.existsByAppointmentIdAndType(1L, NotificationType.APPOINTMENT_REMINDER))
				.thenReturn(false);
		when(jdbcTemplate.queryForObject(any(String.class), eq(Long.class), eq(10L)))
				.thenReturn(100L);

		scheduler.checkUpcomingAppointments();

		verify(notificationRepository).save(any(Notification.class));
	}

	@Test
	void skipsAppointmentsWithExistingReminders() {
		Appointment appointment = mock(Appointment.class);
		when(appointment.getId()).thenReturn(2L);
		when(appointmentRepository.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any()))
				.thenReturn(List.of(appointment));
		when(notificationRepository.existsByAppointmentIdAndType(2L, NotificationType.APPOINTMENT_REMINDER))
				.thenReturn(true);

		scheduler.checkUpcomingAppointments();

		verify(notificationRepository, never()).save(any());
	}

	@Test
	void handlesEmptyAppointmentListGracefully() {
		when(appointmentRepository.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any()))
				.thenReturn(List.of());

		scheduler.checkUpcomingAppointments();

		verify(notificationRepository, never()).save(any());
	}

	@Test
	void onlyQueriesConfirmedStatusAppointments() {
		when(appointmentRepository.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any()))
				.thenReturn(List.of());

		scheduler.checkUpcomingAppointments();

		verify(appointmentRepository).findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any());
	}

	@Test
	void queriesTodayAndTomorrowDateRange() {
		when(appointmentRepository.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), any(), any()))
				.thenReturn(List.of());

		scheduler.checkUpcomingAppointments();

		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);
		verify(appointmentRepository).findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
				eq(AppointmentStatus.CONFIRMED), eq(today), eq(tomorrow));
	}
}
