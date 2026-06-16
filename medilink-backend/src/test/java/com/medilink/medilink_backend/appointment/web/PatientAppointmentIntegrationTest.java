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
class PatientAppointmentIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String patientToken;
	private Long patientId;
	private Long doctorId;

	@BeforeEach
	void setUp() throws Exception {
		String patientEmail = "pat." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		exchange(
				HttpMethod.POST,
				"/api/v1/patients/register",
				null,
				"""
						{
						  "fullName": "Test Patient",
						  "email": "%s",
						  "password": "Patient@123",
						  "phoneNumber": "+15551112233",
						  "dateOfBirth": "1995-06-15",
						  "gender": "MALE",
						  "address": "123 Test Street"
						}
						""".formatted(patientEmail)
		);
		patientToken = login(patientEmail, "Patient@123");

		Long userId = jdbcTemplate.queryForObject(
				"SELECT user_id FROM patients WHERE id = (SELECT MAX(id) FROM patients)", Long.class);
		patientId = jdbcTemplate.queryForObject("SELECT id FROM patients WHERE user_id = ?", Long.class, userId);

		String docEmail = "doc." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Integration", docEmail, passwordEncoder.encode("Doctor@123")
		);
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1
		);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, docUserId);
	}

	@Test
	void patientCanBookAnAppointment() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-01",
						  "startTime": "09:00:00",
						  "reason": "General consultation"
						}
						""".formatted(doctorId)
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals(true, JsonPath.read(response.body(), "$.success"));
		assertEquals("CONFIRMED", JsonPath.read(response.body(), "$.data.status"));
		assertEquals("General consultation", JsonPath.read(response.body(), "$.data.reason"));
		assertNotNull(JsonPath.read(response.body(), "$.data.id"));
	}

	@Test
	void bookingSameSlotTwiceReturnsConflict() throws Exception {
		exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-02",
						  "startTime": "10:00:00",
						  "reason": "First booking"
						}
						""".formatted(doctorId)
		);

		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-02",
						  "startTime": "10:00:00",
						  "reason": "Duplicate booking"
						}
						""".formatted(doctorId)
		);

		assertEquals(HttpStatus.CONFLICT.value(), response.statusCode());
		assertEquals("SLOT_UNAVAILABLE", JsonPath.read(response.body(), "$.error.code"));
	}

	@Test
	void bookingWithPastDateReturnsBadRequest() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2020-01-01",
						  "startTime": "09:00:00",
						  "reason": "Past appointment"
						}
						""".formatted(doctorId)
		);

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
	}

	@Test
	void bookingWithInvalidDoctorReturnsNotFound() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": 99999,
						  "appointmentDate": "2026-09-01",
						  "startTime": "09:00:00",
						  "reason": "Invalid doctor"
						}
						"""
		);

		assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
	}

	@Test
	void patientCanListOwnAppointments() throws Exception {
		exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-03",
						  "startTime": "11:00:00",
						  "reason": "Follow up"
						}
						""".formatted(doctorId)
		);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/appointments",
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertNotNull(JsonPath.read(response.body(), "$.data"));
		assertEquals("Follow up", JsonPath.read(response.body(), "$.data[0].reason"));
	}

	@Test
	void patientCanGetSingleAppointment() throws Exception {
		ApiHttpResponse bookResponse = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-04",
						  "startTime": "14:00:00",
						  "reason": "Single lookup"
						}
						""".formatted(doctorId)
		);
		Long appointmentId = Long.valueOf(JsonPath.read(bookResponse.body(), "$.data.id").toString());

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/appointments/" + appointmentId,
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Single lookup", JsonPath.read(response.body(), "$.data.reason"));
	}

	@Test
	void unauthenticatedUserCannotBookAppointment() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				null,
				"""
						{
						  "doctorId": 1,
						  "appointmentDate": "2026-09-01",
						  "startTime": "09:00:00",
						  "reason": "Unauthenticated"
						}
						"""
		);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	@Test
	void doctorCannotBookAsPatient() throws Exception {
		String docEmail = "doc2." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Cannot Book", docEmail, passwordEncoder.encode("Doctor@123")
		);
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1
		);
		String doctorToken = login(docEmail, "Doctor@123");

		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				doctorToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-09-02",
						  "startTime": "10:00:00",
						  "reason": "Doctor booking as patient"
						}
						""".formatted(doctorId)
		);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
	}

	@Test
	void overlappingTimeSlotReturnsConflict() throws Exception {
		exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-05",
						  "startTime": "15:00:00",
						  "reason": "First booking"
						}
						""".formatted(doctorId)
		);

		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "doctorId": %d,
						  "appointmentDate": "2026-08-05",
						  "startTime": "15:15:00",
						  "reason": "Overlapping booking"
						}
						""".formatted(doctorId)
		);

		assertEquals(HttpStatus.CONFLICT.value(), response.statusCode());
		assertEquals("SLOT_UNAVAILABLE", JsonPath.read(response.body(), "$.error.code"));
	}

	@Test
	void bookingMissingRequiredFieldsReturnsBadRequest() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patient/appointments",
				patientToken,
				"""
						{
						  "appointmentDate": "2026-09-01",
						  "startTime": "09:00:00",
						  "reason": "Missing doctorId"
						}
						"""
		);

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
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
