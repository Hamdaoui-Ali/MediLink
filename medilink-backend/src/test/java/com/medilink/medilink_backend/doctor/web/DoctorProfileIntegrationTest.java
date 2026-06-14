package com.medilink.medilink_backend.doctor.web;

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
class DoctorProfileIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String doctorToken;
	private Long doctorUserId;

	@BeforeEach
	void setUp() throws Exception {
		String email = "dr.profile." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";

		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, phone_number, account_status) VALUES (?, ?, ?, ?, '+15551111111', 'ACTIVE')",
				2, "Dr. Profile Test", email, passwordEncoder.encode("Doctor@123")
		);
		doctorUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);

		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, biography, clinic_address, status) VALUES (?, ?, 30, 'Original bio', 'Original address', 'ACTIVE')",
				doctorUserId, 1
		);

		doctorToken = login(email, "Doctor@123");
	}

	@Test
	void doctorCanViewOwnProfile() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/profile", doctorToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Dr. Profile Test", JsonPath.read(response.body(), "$.data.fullName"));
		assertEquals("Original bio", JsonPath.read(response.body(), "$.data.biography"));
		assertEquals("Original address", JsonPath.read(response.body(), "$.data.clinicAddress"));
		assertEquals(30, (int) JsonPath.read(response.body(), "$.data.consultationDurationMinutes"));
		assertEquals("ACTIVE", JsonPath.read(response.body(), "$.data.status"));
		assertNotNull(JsonPath.read(response.body(), "$.data.specialtyName"));
	}

	@Test
	void doctorCanUpdateOwnProfile() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/profile",
				doctorToken,
				"""
						{
						  "biography": "Updated bio",
						  "clinicAddress": "456 New Clinic",
						  "consultationDurationMinutes": 45,
						  "phoneNumber": "+15559999999"
						}
						"""
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Updated bio", JsonPath.read(response.body(), "$.data.biography"));
		assertEquals("456 New Clinic", JsonPath.read(response.body(), "$.data.clinicAddress"));
		assertEquals(45, (int) JsonPath.read(response.body(), "$.data.consultationDurationMinutes"));

		// Verify phone number was persisted
		String phone = jdbcTemplate.queryForObject(
				"SELECT phone_number FROM users WHERE id = ?", String.class, doctorUserId);
		assertEquals("+15559999999", phone);
	}

	@Test
	void doctorCannotChangeStatusOrSpecialtyThroughProfileUpdate() throws Exception {
		exchange(
				HttpMethod.PATCH,
				"/api/v1/doctor/profile",
				doctorToken,
				"""
						{
						  "biography": "New bio"
						}
						"""
		);

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/profile", doctorToken, null);

		// Status and specialty should be unchanged
		assertEquals("ACTIVE", JsonPath.read(response.body(), "$.data.status"));
	}

	@Test
	void unauthenticatedUserCannotAccessProfile() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/profile", null, null);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	@Test
	void patientCannotAccessDoctorProfile() throws Exception {
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

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/profile", patientToken, null);

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
