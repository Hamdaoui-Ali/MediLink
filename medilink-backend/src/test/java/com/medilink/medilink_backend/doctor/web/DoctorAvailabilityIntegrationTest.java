package com.medilink.medilink_backend.doctor.web;

import com.jayway.jsonpath.JsonPath;
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

	@Test
	void unauthorizedUsersCannotAccessAvailabilityEndpoints() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/availability", null, null);
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	@Test
	void patientRoleCannotAccessDoctorAvailability() throws Exception {
		String patientToken = registerAndLoginPatient();

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/doctor/availability", patientToken, null);
		assertEquals(HttpStatus.FORBIDDEN.value(), response.statusCode());
	}

	@Test
	void doctorCanManageAvailabilityLifecycle() throws Exception {
		String doctorEmail = "dr.avail." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		String doctorToken = createDoctorAndLogin(doctorEmail, "Dr. Availability Tester");

		ApiHttpResponse listEmpty = exchange(HttpMethod.GET, "/api/v1/doctor/availability", doctorToken, null);
		assertEquals(HttpStatus.OK.value(), listEmpty.statusCode());
		assertEquals(0, ((java.util.List<?>) JsonPath.read(listEmpty.body(), "$.data")).size());

		ApiHttpResponse created = exchange(
				HttpMethod.POST,
				"/api/v1/doctor/availability",
				doctorToken,
				"""
						{"dayOfWeek": 1, "startTime": "09:00:00", "endTime": "17:00:00"}
						"""
		);
		assertEquals(HttpStatus.CREATED.value(), created.statusCode());
		assertEquals(Integer.valueOf(1), JsonPath.read(created.body(), "$.data.dayOfWeek"));
		assertEquals("09:00:00", JsonPath.read(created.body(), "$.data.startTime"));
		Number slotId = JsonPath.read(created.body(), "$.data.id");

		ApiHttpResponse list = exchange(HttpMethod.GET, "/api/v1/doctor/availability", doctorToken, null);
		assertEquals(HttpStatus.OK.value(), list.statusCode());
		assertEquals(1, ((java.util.List<?>) JsonPath.read(list.body(), "$.data")).size());

		ApiHttpResponse updated = exchange(
				HttpMethod.PUT,
				"/api/v1/doctor/availability/" + slotId.longValue(),
				doctorToken,
				"""
						{"dayOfWeek": 3, "startTime": "14:00:00", "endTime": "18:00:00"}
						"""
		);
		assertEquals(HttpStatus.OK.value(), updated.statusCode());
		assertEquals(Integer.valueOf(3), JsonPath.read(updated.body(), "$.data.dayOfWeek"));

		ApiHttpResponse deactivated = exchange(
				HttpMethod.DELETE,
				"/api/v1/doctor/availability/" + slotId.longValue(),
				doctorToken,
				null
		);
		assertEquals(HttpStatus.OK.value(), deactivated.statusCode());
		assertEquals(false, JsonPath.read(deactivated.body(), "$.data.isActive"));

		ApiHttpResponse listAfterDelete = exchange(HttpMethod.GET, "/api/v1/doctor/availability", doctorToken, null);
		assertEquals(0, ((java.util.List<?>) JsonPath.read(listAfterDelete.body(), "$.data")).size());
	}

	@Test
	void doctorCannotAccessAnotherDoctorsSlots() throws Exception {
		String email1 = "dr.one." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		String token1 = createDoctorAndLogin(email1, "Dr. One");
		String email2 = "dr.two." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		String token2 = createDoctorAndLogin(email2, "Dr. Two");

		ApiHttpResponse created = exchange(
				HttpMethod.POST,
				"/api/v1/doctor/availability",
				token1,
				"""
						{"dayOfWeek": 1, "startTime": "09:00:00", "endTime": "12:00:00"}
						"""
		);
		Number slotId = JsonPath.read(created.body(), "$.data.id");

		ApiHttpResponse otherDoctorTriesToUpdate = exchange(
				HttpMethod.PUT,
				"/api/v1/doctor/availability/" + slotId.longValue(),
				token2,
				"""
						{"dayOfWeek": 5, "startTime": "10:00:00", "endTime": "14:00:00"}
						"""
		);
		assertEquals(HttpStatus.NOT_FOUND.value(), otherDoctorTriesToUpdate.statusCode());
	}

	@Test
	void invalidTimeRangeReturnsBadRequest() throws Exception {
		String token = createDoctorAndLogin(
				"dr.bad." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local",
				"Dr. Bad Time"
		);

		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/doctor/availability",
				token,
				"""
						{"dayOfWeek": 1, "startTime": "17:00:00", "endTime": "09:00:00"}
						"""
		);
		assertEquals(HttpStatus.BAD_REQUEST.value(), response.statusCode());
		assertEquals("INVALID_AVAILABILITY", JsonPath.read(response.body(), "$.error.code"));
	}

	private String createDoctorAndLogin(String email, String fullName) throws Exception {
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, fullName, email, passwordEncoder.encode("Doctor@123")
		);
		Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, email);

		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				userId, 1
		);

		return login(email, "Doctor@123");
	}

	private String registerAndLoginPatient() throws Exception {
		String email = "patient." + java.util.UUID.randomUUID() + "@example.com";
		ApiHttpResponse response = exchange(
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
						""".formatted(email)
		);
		assertEquals(HttpStatus.CREATED.value(), response.statusCode());
		return login(email, "Patient@123");
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
