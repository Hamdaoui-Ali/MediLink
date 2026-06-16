package com.medilink.medilink_backend.blockedslot.web;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class DoctorBlockedSlotIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String doctorToken;
	private Long doctorId;

	@BeforeEach
	void setUp() throws Exception {
		String email = "dr.block." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		String fullName = "Dr. Block Tester";

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

		doctorToken = login(email, "Doctor@123");
	}

	@Test
	void doctorCanListOwnBlockedSlots() throws Exception {
		jdbcTemplate.update(
				"INSERT INTO blocked_slots (doctor_id, block_date, start_time, end_time, reason, is_active) VALUES (?, '2026-06-20', '10:00:00', '12:00:00', 'Vacation', TRUE)",
				doctorId
		);

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/blocked-slots", doctorToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<?> data = JsonPath.read(response.body(), "$.data");
		assertEquals(1, ((List<?>) data).size());
	}

	@Test
	void doctorCanCreateBlockedSlot() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/doctor/blocked-slots",
				doctorToken,
				"""
						{
						  "blockDate": "2026-07-15",
						  "startTime": "14:00:00",
						  "endTime": "16:00:00",
						  "reason": "Conference"
						}
						"""
		);

		assertEquals(HttpStatus.CREATED.value(), response.statusCode());
		assertEquals("Conference", JsonPath.read(response.body(), "$.data.reason"));
	}

	@Test
	void doctorCanDeleteBlockedSlot() throws Exception {
		jdbcTemplate.update(
				"INSERT INTO blocked_slots (doctor_id, block_date, start_time, end_time, reason, is_active) VALUES (?, '2026-06-20', '10:00:00', '12:00:00', 'Vacation', TRUE)",
				doctorId
		);
		Long slotId = jdbcTemplate.queryForObject(
				"SELECT id FROM blocked_slots WHERE doctor_id = ? ORDER BY id DESC LIMIT 1", Long.class, doctorId);

		ApiHttpResponse response = exchange(
				HttpMethod.DELETE,
				"/api/v1/doctor/blocked-slots/" + slotId,
				doctorToken,
				null
		);

		assertEquals(HttpStatus.NO_CONTENT.value(), response.statusCode());

		Boolean isActive = jdbcTemplate.queryForObject(
				"SELECT is_active FROM blocked_slots WHERE id = ?", Boolean.class, slotId);
		assertEquals(false, isActive);
	}

	@Test
	void cannotDeleteOtherDoctorsBlockedSlot() throws Exception {
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

		jdbcTemplate.update(
				"INSERT INTO blocked_slots (doctor_id, block_date, start_time, end_time, reason, is_active) VALUES (?, '2026-06-20', '10:00:00', '12:00:00', 'Vacation', TRUE)",
				doctorId
		);
		Long slotId = jdbcTemplate.queryForObject(
				"SELECT id FROM blocked_slots WHERE doctor_id = ? ORDER BY id DESC LIMIT 1", Long.class, doctorId);

		ApiHttpResponse response = exchange(
				HttpMethod.DELETE,
				"/api/v1/doctor/blocked-slots/" + slotId,
				otherToken,
				null
		);

		assertEquals(HttpStatus.NOT_FOUND.value(), response.statusCode());
	}

	@Test
	void createBlockedSlotRejectsInvalidTimeRange() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/doctor/blocked-slots",
				doctorToken,
				"""
						{
						  "blockDate": "2026-07-15",
						  "startTime": "16:00:00",
						  "endTime": "14:00:00",
						  "reason": "Bad range"
						}
						"""
		);

		assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
		assertEquals("INVALID_BLOCKED_SLOT", JsonPath.read(response.body(), "$.error.code"));
	}

	@Test
	void doctorCanUpdateBlockedSlot() throws Exception {
		jdbcTemplate.update(
				"INSERT INTO blocked_slots (doctor_id, block_date, start_time, end_time, reason, is_active) VALUES (?, '2026-06-20', '10:00:00', '12:00:00', 'Vacation', TRUE)",
				doctorId
		);
		Long slotId = jdbcTemplate.queryForObject(
				"SELECT id FROM blocked_slots WHERE doctor_id = ? ORDER BY id DESC LIMIT 1", Long.class, doctorId);

		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/blocked-slots/" + slotId,
				doctorToken,
				"""
						{
						  "blockDate": "2026-07-01",
						  "startTime": "09:00:00",
						  "endTime": "10:00:00",
						  "reason": "Updated reason"
						}
						"""
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Updated reason", JsonPath.read(response.body(), "$.data.reason"));
		assertEquals("2026-07-01", JsonPath.read(response.body(), "$.data.blockDate"));
	}

	@Test
	void unauthenticatedUserCannotAccessBlockedSlots() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/blocked-slots", null, null);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
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
