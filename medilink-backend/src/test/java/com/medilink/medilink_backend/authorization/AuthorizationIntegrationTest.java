package com.medilink.medilink_backend.authorization;

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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class AuthorizationIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private String adminToken;
	private String doctorToken;
	private String patientToken;
	private Long doctorId;

	@BeforeEach
	void setUp() throws Exception {
		String adminEmail = "auth.admin." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				1, "Auth Admin", adminEmail, passwordEncoder.encode("Admin@12345"));
		adminToken = login(adminEmail, "Admin@12345");

		String docEmail = "auth.doc." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Auth Doctor", docEmail, passwordEncoder.encode("Doctor@123"));
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				docUserId, 1);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, docUserId);
		doctorToken = login(docEmail, "Doctor@123");

		String patEmail = "auth.pat." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		exchange(HttpMethod.POST, "/api/v1/patients/register", null, """
				{
				  "fullName": "Auth Patient",
				  "email": "%s",
				  "password": "Patient@123",
				  "phoneNumber": "+15550000001",
				  "dateOfBirth": "1995-01-01",
				  "gender": "FEMALE"
				}
				""".formatted(patEmail));
		patientToken = login(patEmail, "Patient@123");
	}

	@Test
	void unauthenticatedAccessToProtectedEndpointsReturns401() throws Exception {
		assertEquals(HttpStatus.UNAUTHORIZED.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/appointments", null, null).statusCode());
		assertEquals(HttpStatus.UNAUTHORIZED.value(),
				exchange(HttpMethod.GET, "/api/v1/doctor/appointments", null, null).statusCode());
		assertEquals(HttpStatus.UNAUTHORIZED.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/doctors", null, null).statusCode());
		assertEquals(HttpStatus.UNAUTHORIZED.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/doctors/" + doctorId + "/slots?date=2026-08-01", null, null).statusCode());
	}

	@Test
	void doctorCannotAccessPatientEndpoints() throws Exception {
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/appointments", doctorToken, null).statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.POST, "/api/v1/patient/appointments", doctorToken,
						"{\"doctorId\":1,\"appointmentDate\":\"2026-09-01\",\"startTime\":\"09:00:00\",\"reason\":\"X\"}").statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/doctors", doctorToken, null).statusCode());
	}

	@Test
	void patientCannotAccessDoctorEndpoints() throws Exception {
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/doctor/appointments", patientToken, null).statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.PATCH, "/api/v1/doctor/appointments/1/status", patientToken,
						"{\"status\":\"COMPLETED\"}").statusCode());
	}

	@Test
	void patientCannotAccessAdminEndpoints() throws Exception {
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/specialties?activeOnly=false", patientToken, null).statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.POST, "/api/v1/specialties", patientToken,
						"{\"name\":\"Test\",\"description\":\"Test\"}").statusCode());
	}

	@Test
	void doctorCannotAccessAdminEndpoints() throws Exception {
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/specialties?activeOnly=false", doctorToken, null).statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.POST, "/api/v1/specialties", doctorToken,
						"{\"name\":\"Test\",\"description\":\"Test\"}").statusCode());
	}

	@Test
	void adminCannotAccessRoleSpecificEndpoints() throws Exception {
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/doctor/appointments", adminToken, null).statusCode());
		assertEquals(HttpStatus.FORBIDDEN.value(),
				exchange(HttpMethod.GET, "/api/v1/patient/appointments", adminToken, null).statusCode());
	}

	@Test
	void publicEndpointsAreAccessibleWithoutAuth() throws Exception {
		assertEquals(HttpStatus.OK.value(),
				exchange(HttpMethod.GET, "/api/v1/specialties", null, null).statusCode());
		assertEquals(HttpStatus.OK.value(),
				exchange(HttpMethod.GET, "/api/v1/health", null, null).statusCode());
	}

	private String login(String email, String password) throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.POST, "/api/v1/auth/login", null,
				"{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}");
		assertEquals(HttpStatus.OK.value(), response.statusCode());
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
