package com.medilink.medilink_backend.notification.scheduler;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AppointmentSchedulerIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AppointmentScheduler appointmentScheduler;

	private String patientToken;
	private Long doctorId;
	private Long patientId;

	@BeforeEach
	void setUp() throws Exception {
		String patientEmail = "pat.sched." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		exchange(HttpMethod.POST, "/api/v1/patients/register", null, """
				{
				  "fullName": "Scheduler Patient",
				  "email": "%s",
				  "password": "Patient@123",
				  "phoneNumber": "+15556667777",
				  "dateOfBirth": "1993-01-15",
				  "gender": "FEMALE",
				  "address": "Scheduler Street"
				}
				""".formatted(patientEmail));
		patientToken = login(patientEmail, "Patient@123");

		String docEmail = "doc.sched." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Scheduler", docEmail, passwordEncoder.encode("Doctor@123"));
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, docUserId);

		Long patientUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, patientEmail);
		patientId = jdbcTemplate.queryForObject("SELECT id FROM patients WHERE user_id = ?", Long.class, patientUserId);
	}

	@Test
	void schedulerCreatesNotificationRecordsForTodayAndTomorrowConfirmedAppointments() throws Exception {
		LocalDate today = LocalDate.now();
		LocalDate tomorrow = today.plusDays(1);

		Long pId = patientId;
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '09:00:00', '09:30:00', 'CONFIRMED', 'Scheduled today')",
				doctorId, pId, today);
		Long appt1Id = jdbcTemplate.queryForObject(
				"SELECT id FROM appointments WHERE reason = 'Scheduled today'", Long.class);
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '10:00:00', '10:30:00', 'CONFIRMED', 'Scheduled tomorrow')",
				doctorId, pId, tomorrow);
		Long appt2Id = jdbcTemplate.queryForObject(
				"SELECT id FROM appointments WHERE reason = 'Scheduled tomorrow'", Long.class);

		appointmentScheduler.checkUpcomingAppointments();

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER' AND appointment_id IN (?, ?)",
				Integer.class, appt1Id, appt2Id);
		assertEquals(2, count);
	}

	@Test
	void schedulerSkipsAppointmentsThatAlreadyHaveReminders() throws Exception {
		LocalDate today = LocalDate.now();

		Long pId = patientId;
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '11:00:00', '11:30:00', 'CONFIRMED', 'Today appointment')",
				doctorId, patientId, today);
		Long appointmentId = jdbcTemplate.queryForObject(
				"SELECT id FROM appointments WHERE reason = 'Today appointment'", Long.class);

		jdbcTemplate.update(
				"INSERT INTO notifications (user_id, appointment_id, type, status, recipient_email, subject) VALUES (?, ?, 'APPOINTMENT_REMINDER', 'PENDING', 'test@medilink.local', 'Reminder')",
				patientId, appointmentId);

		appointmentScheduler.checkUpcomingAppointments();

		Integer count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER' AND appointment_id = ?",
				Integer.class, appointmentId);
		assertEquals(1, count);
	}

	@Test
	void schedulerSkipsNonConfirmedAppointments() throws Exception {
		LocalDate today = LocalDate.now();

		Long pId = patientId;
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '12:00:00', '12:30:00', 'CANCELLED', 'Cancelled appointment')",
				doctorId, patientId, today);
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '13:00:00', '13:30:00', 'COMPLETED', 'Completed appointment')",
				doctorId, patientId, today);

		Integer before = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER'", Integer.class);

		appointmentScheduler.checkUpcomingAppointments();

		Integer after = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER'", Integer.class);
		assertEquals(before, after);
	}

	@Test
	void schedulerCreatesRecordsWithPendingStatus() throws Exception {
		LocalDate today = LocalDate.now();
		Long pId = patientId;
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '14:00:00', '14:30:00', 'CONFIRMED', 'Pending reminder test')",
				doctorId, patientId, today);

		appointmentScheduler.checkUpcomingAppointments();

		String status = jdbcTemplate.queryForObject(
				"SELECT status FROM notifications WHERE type = 'APPOINTMENT_REMINDER' ORDER BY id DESC LIMIT 1",
				String.class);
		assertEquals("PENDING", status);
	}

	@Test
	void schedulerSkipsAppointmentsOutsideTodayAndTomorrow() throws Exception {
		LocalDate futureDate = LocalDate.now().plusDays(3);

		Long pId = patientId;
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '15:00:00', '15:30:00', 'CONFIRMED', 'Future appointment')",
				doctorId, patientId, futureDate);

		Integer before = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER'", Integer.class);

		appointmentScheduler.checkUpcomingAppointments();

		Integer after = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM notifications WHERE type = 'APPOINTMENT_REMINDER'", Integer.class);
		assertEquals(before, after);
	}

	private String login(String email, String password) throws Exception {
		String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
		ApiHttpResponse response = exchange(HttpMethod.POST, "/api/v1/auth/login", null, body);
		return JsonPath.read(response.body(), "$.data.accessToken");
	}

	private ApiHttpResponse exchange(HttpMethod method, String path, String token, String body) throws IOException, InterruptedException {
		HttpRequest.Builder req = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(method.name(), body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		if (token != null) req.header("Authorization", "Bearer " + token);
		HttpResponse<String> resp = httpClient.send(req.build(), HttpResponse.BodyHandlers.ofString());
		return new ApiHttpResponse(resp.statusCode(), resp.body());
	}

	private record ApiHttpResponse(int statusCode, String body) {}
}
