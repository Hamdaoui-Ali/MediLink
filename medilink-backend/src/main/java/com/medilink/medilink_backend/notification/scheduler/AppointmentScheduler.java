package com.medilink.medilink_backend.notification.scheduler;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.notification.domain.Notification;
import com.medilink.medilink_backend.notification.domain.NotificationType;
import com.medilink.medilink_backend.notification.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AppointmentScheduler {

	private static final Logger log = LoggerFactory.getLogger(AppointmentScheduler.class);

	private final AppointmentRepository appointmentRepository;
	private final NotificationRepository notificationRepository;
	private final JdbcTemplate jdbcTemplate;

	public AppointmentScheduler(
			AppointmentRepository appointmentRepository,
			NotificationRepository notificationRepository,
			JdbcTemplate jdbcTemplate
	) {
		this.appointmentRepository = appointmentRepository;
		this.notificationRepository = notificationRepository;
		this.jdbcTemplate = jdbcTemplate;
	}

	@Scheduled(fixedRate = 30 * 60 * 1000)
	@Transactional
	public void checkUpcomingAppointments() {
		log.info("Running scheduled appointment check");

		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		List<Appointment> appointments = appointmentRepository
				.findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
						AppointmentStatus.CONFIRMED, today, tomorrow);

		int created = 0;
		int skipped = 0;

		for (Appointment appointment : appointments) {
			if (notificationRepository.existsByAppointmentIdAndType(
					appointment.getId(), NotificationType.APPOINTMENT_REMINDER)) {
				skipped++;
				continue;
			}

			Long userId = jdbcTemplate.queryForObject(
					"SELECT user_id FROM patients WHERE id = ?", Long.class,
					appointment.getPatientId());

			Notification notification = new Notification(
					userId,
					appointment.getId(),
					NotificationType.APPOINTMENT_REMINDER,
					"pending@medilink.local",
					"Upcoming appointment reminder"
			);
			notificationRepository.save(notification);
			created++;
		}

		log.info("Appointment check complete: {} tracking records created, {} already existed (skipped)",
				created, skipped);
	}
}
