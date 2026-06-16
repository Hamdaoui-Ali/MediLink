package com.medilink.medilink_backend.availability.web;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class DoctorAvailabilityIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String patientToken;
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
						  "fullName": "Slot Test Patient",
						  "email": "%s",
						  "password": "Patient@123",
						  "phoneNumber": "+15559998888",
						  "dateOfBirth": "1992-08-20",
						  "gender": "FEMALE",
						  "address": "456 Slot Street"
						}
						""".formatted(patientEmail)
		);
		patientToken = login(patientEmail, "Patient@123");

		String docEmail = "doc." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Slots", docEmail, passwordEncoder.encode("Doctor@123")
		);
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1
		);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, docUserId);
	}

	@Test
	void returnsSlotsForDayWithAvailability() throws Exception {
		LocalDate monday = nextDayOfWeek(java.time.DayOfWeek.MONDAY);
		int dayOfWeek = monday.getDayOfWeek().getValue();

		jdbcTemplate.update(
				"INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, is_active) VALUES (?, ?, '09:00:00', '12:00:00', TRUE)",
				doctorId, dayOfWeek
		);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + monday,
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<String> startTimes = JsonPath.read(response.body(), "$.data[*].startTime");
		assertEquals(6, startTimes.size());
		assertEquals("09:00:00", startTimes.getFirst());
		assertEquals("11:30:00", startTimes.getLast());
	}

	@Test
	void returnsEmptyListWhenNoAvailability() throws Exception {
		LocalDate wednesday = nextDayOfWeek(java.time.DayOfWeek.WEDNESDAY);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + wednesday,
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<?> data = JsonPath.read(response.body(), "$.data");
		assertEquals(0, ((List<?>) data).size());
	}

	@Test
	void slotsAreExcludedWhenBlocked() throws Exception {
		LocalDate friday = nextDayOfWeek(java.time.DayOfWeek.FRIDAY);
		int dayOfWeek = friday.getDayOfWeek().getValue();

		jdbcTemplate.update(
				"INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, is_active) VALUES (?, ?, '08:00:00', '12:00:00', TRUE)",
				doctorId, dayOfWeek
		);

		jdbcTemplate.update(
				"INSERT INTO blocked_slots (doctor_id, block_date, start_time, end_time, reason) VALUES (?, ?, '08:00:00', '09:00:00', 'Meeting')",
				doctorId, friday
		);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + friday,
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<String> startTimes = JsonPath.read(response.body(), "$.data[*].startTime");
		assertEquals(6, startTimes.size());
		assertEquals("09:00:00", startTimes.getFirst());
	}

	@Test
	void slotsAreExcludedWhenBooked() throws Exception {
		LocalDate tuesday = nextDayOfWeek(java.time.DayOfWeek.TUESDAY);
		int dayOfWeek = tuesday.getDayOfWeek().getValue();

		jdbcTemplate.update(
				"INSERT INTO doctor_availability (doctor_id, day_of_week, start_time, end_time, is_active) VALUES (?, ?, '10:00:00', '14:00:00', TRUE)",
				doctorId, dayOfWeek
		);

		Long patientId = jdbcTemplate.queryForObject("SELECT id FROM patients LIMIT 1", Long.class);
		jdbcTemplate.update(
				"INSERT INTO appointments (doctor_id, patient_id, appointment_date, start_time, end_time, status, reason) VALUES (?, ?, ?, '11:00:00', '11:30:00', 'CONFIRMED', 'Taken slot')",
				doctorId, patientId, tuesday
		);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + tuesday,
				patientToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<String> startTimes = JsonPath.read(response.body(), "$.data[*].startTime");
		assertEquals(7, startTimes.size());
	}

	@Test
	void unauthenticatedUserCannotAccessSlots() throws Exception {
		LocalDate monday = nextDayOfWeek(java.time.DayOfWeek.MONDAY);

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + monday,
				null,
				null
		);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	@Test
	void doctorCannotAccessPatientSlotsEndpoint() throws Exception {
		String docEmail = "doc2." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Cannot", docEmail, passwordEncoder.encode("Doctor@123")
		);
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1
		);
		String doctorToken = login(docEmail, "Doctor@123");

		LocalDate monday = nextDayOfWeek(java.time.DayOfWeek.MONDAY);
		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/patient/doctors/" + doctorId + "/slots?date=" + monday,
				doctorToken,
				null
		);

		assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
	}

	private LocalDate nextDayOfWeek(java.time.DayOfWeek target) {
		LocalDate today = LocalDate.now();
		int daysUntil = target.getValue() - today.getDayOfWeek().getValue();
		if (daysUntil <= 0) {
			daysUntil += 7;
		}
		return today.plusDays(daysUntil);
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
