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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class PatientDoctorSearchIntegrationTest {

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
		exchange(HttpMethod.POST, "/api/v1/patients/register", null, """
				{
				  "fullName": "Search Patient",
				  "email": "%s",
				  "password": "Patient@123",
				  "phoneNumber": "+15550001111",
				  "dateOfBirth": "1990-01-01",
				  "gender": "MALE",
				  "address": "Search Street"
				}
				""".formatted(patientEmail));
		patientToken = login(patientEmail, "Patient@123");

		String docEmail = "doc." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Search Test", docEmail, passwordEncoder.encode("Doctor@123"));
		Long docUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, biography, clinic_address, status) VALUES (?, ?, 30, 'Test bio', '123 Clinic', 'ACTIVE')",
				docUserId, 1);
		doctorId = jdbcTemplate.queryForObject("SELECT id FROM doctors WHERE user_id = ?", Long.class, docUserId);
	}

	@Test
	void patientCanListActiveDoctors() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/patient/doctors", patientToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<?> data = JsonPath.read(response.body(), "$.data");
		assertEquals(true, ((List<?>) data).size() > 0);
	}

	@Test
	void patientCanFilterDoctorsBySpecialty() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.GET, "/api/v1/patient/doctors?specialtyId=1", patientToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<String> names = JsonPath.read(response.body(), "$.data[*].specialtyName");
		for (String name : names) {
			assertEquals("General Medicine", name);
		}
	}

	@Test
	void patientCanFilterDoctorsByName() throws Exception {
		String uniqueName = "UniqueSearchTerm" + java.util.UUID.randomUUID().toString().substring(0, 4);
		String docEmail = "doc.filter." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, uniqueName, docEmail, passwordEncoder.encode("Doctor@123"));
		Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, docEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'ACTIVE')",
				userId, 2);

		ApiHttpResponse response = exchange(
				HttpMethod.GET, "/api/v1/patient/doctors?name=" + uniqueName, patientToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<String> names = JsonPath.read(response.body(), "$.data[*].fullName");
		assertEquals(1, names.size());
		assertEquals(uniqueName, names.getFirst());
	}

	@Test
	void patientCanViewDoctorDetail() throws Exception {
		ApiHttpResponse response = exchange(
				HttpMethod.GET, "/api/v1/patient/doctors/" + doctorId, patientToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals("Dr. Search Test", JsonPath.read(response.body(), "$.data.fullName"));
		assertEquals("General Medicine", JsonPath.read(response.body(), "$.data.specialtyName"));
	}

	@Test
	void inactiveDoctorsDoNotAppearInSearch() throws Exception {
		String inactiveEmail = "inactive." + java.util.UUID.randomUUID().toString().substring(0, 8) + "@medilink.local";
		jdbcTemplate.update(
				"INSERT INTO users (role_id, full_name, email, password_hash, account_status) VALUES (?, ?, ?, ?, 'ACTIVE')",
				2, "Dr. Inactive", inactiveEmail, passwordEncoder.encode("Doctor@123"));
		Long inactiveUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, inactiveEmail);
		jdbcTemplate.update(
				"INSERT INTO doctors (user_id, specialty_id, consultation_duration_minutes, status) VALUES (?, ?, 30, 'INACTIVE')",
				inactiveUserId, 1);

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/patient/doctors?name=Inactive", patientToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		List<?> data = JsonPath.read(response.body(), "$.data");
		assertEquals(0, ((List<?>) data).size());
	}

	@Test
	void unauthenticatedUserCannotSearchDoctors() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/patient/doctors", null, null);
		assertEquals(HttpStatus.UNAUTHORIZED.value(), response.statusCode());
	}

	private String login(String email, String password) throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.POST, "/api/v1/auth/login", null, """
				{ "email": "%s", "password": "%s" }
				""".formatted(email, password));
		assertEquals(HttpStatus.OK.value(), response.statusCode());
		return JsonPath.read(response.body(), "$.data.accessToken");
	}

	private ApiHttpResponse exchange(HttpMethod method, String path, String token, String body) throws IOException, InterruptedException {
		HttpRequest.Builder request = HttpRequest.newBuilder()
				.uri(URI.create("http://localhost:" + port + path))
				.header("Content-Type", "application/json")
				.method(method.name(), body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		if (token != null) request.header("Authorization", "Bearer " + token);
		HttpResponse<String> response = httpClient.send(request.build(), HttpResponse.BodyHandlers.ofString());
		return new ApiHttpResponse(response.statusCode(), response.body());
	}

	private record ApiHttpResponse(int statusCode, String body) {}
}
