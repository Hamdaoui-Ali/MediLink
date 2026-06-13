package com.medilink.medilink_backend.appointment.web;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class DoctorAppointmentIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String doctorToken;
	private Long doctorId;
	private Long appointmentId;

	@BeforeEach
	void setUp() throws Exception {
		String email = "dr.appt." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		String fullName = "Dr. Appointment Tester";

		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, fullName, email, passwordEncoder.encode("Doctor@123")
		);
		Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);

		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				userId, 1
		);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, userId);

		Long patientId = jdbcTemplate.queryForObject("SELECT id FROM patients LIMIT 1", Long.class);
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, '2026-06-20', '10:00:00', '10:30:00', 'CONFIRMED', 'Routine checkup')",
				doctorId, patientId
		);
		appointmentId = jdbcTemplate.queryForObject("SELECT id FROM appointments WHERE doctor_id = ? ORDER BY id DESC LIMIT 1", Long.class, doctorId);

		doctorToken = login(email, "Doctor@123");
	}

	@Test
	void doctorCanListOwnAppointments() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/appointments", doctorToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertNotNull(JsonPath.read(response.body(), "$.data"));
	}

	@Test
	void doctorCanViewOwnAppointment() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/doctor/appointments/" + appointmentId,
				doctorToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("CONFIRMED", JsonPath.read(response.body(), "$.data.status"));
	}

	@Test
	void doctorCanUpdateNotesOnOwnAppointment() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/appointments/" + appointmentId + "/notes",
				doctorToken,
				"""
						{"notes": "Patient vitals are normal."}
						"""
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Patient vitals are normal.", JsonPath.read(response.body(), "$.data.doctorNotes"));
	}

	@Test
	void doctorCanMarkAppointmentAsCompleted() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/appointments/" + appointmentId + "/status",
				doctorToken,
				"""
						{"status": "COMPLETED"}
						"""
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("COMPLETED", JsonPath.read(response.body(), "$.data.status"));
	}

	@Test
	void doctorCannotUpdateOtherDoctorsAppointments() throws Exception {
		String otherEmail = "dr.other." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Other", otherEmail, passwordEncoder.encode("Doctor@123")
		);
		Long otherUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, otherEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				otherUserId, 1
		);
		String otherToken = login(otherEmail, "Doctor@123");

		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/appointments/" + appointmentId + "/status",
				otherToken,
				"""
						{"status": "COMPLETED"}
						"""
		);

		assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
	}

	@Test
	void invalidStatusTransitionReturnsBadRequest() throws Exception {
		exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/appointments/" + appointmentId + "/status",
				doctorToken,
				"""
						{"status": "COMPLETED"}
						"""
		);

		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/appointments/" + appointmentId + "/status",
				doctorToken,
				"""
						{"status": "CONFIRMED"}
						"""
		);

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
		assertEquals("INVALID_STATUS_TRANSITION", JsonPath.read(response.body(), "$.error.code"));
	}

	@Test
	void unauthenticatedUserCannotAccessAppointmentEndpoints() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/appointments", null, null);
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	@Test
	void patientCannotAccessDoctorAppointmentEndpoints() throws Exception {
		String patientEmail = "patient." + java.util.UUID.randomUUID() + "@example.com";
		exchange(
				HttpMethod.POST,
				"/api/v1/patients/register",
				null,
				"""
						{
						  "fullName": "Jane Patient",
						  "email": "%s",
						  "password": "Patient@123",
						  "phoneNumber": "+15551234567",
						  "dateOfBirth": "1990-03-05",
						  "gender": "FEMALE",
						  "address": "100 Care Street"
						}
						""".formatted(patientEmail)
		);
		String patientToken = login(patientEmail, "Patient@123");

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/appointments", patientToken, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
	}

	private String login(String email, String password) throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/auth/login",
				null,
				"""
						{
						  "email": "%s",
						  "password": "%s"
						}
						""".formatted(email, password)
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		return JsonPath.read(response.body(), "$.data.accessToken");
	}

	private ApiHttpResponse exchange(HttpMethod method, String path, String token, String body) throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(method.name(), body == null
						? HttpRequest.BodyPublishers.noBody()
						: HttpRequest.BodyPublishers.ofString(body));

		if (token != null) {
			request.header("Authorization", "Bearer " + token);
		}

		HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
		return new ApiHttpResponse(response.statusCode(), response.body());
	}

	private record ApiHttpResponse(int statusCode, String body) {}
}
