package com.medilink.medilink_backend.doctor.web;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DoctorManagementIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Test
	void adminCanCreateEditDeactivateAndReactivateDoctorThroughHttpApi() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");
		Number specialtyId = firstActiveSpecialtyId();
		String email = "doctor." + UUID.randomUUID() + "@example.com";

		ApiHttpResponse created = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				adminToken,
				doctorJson("  Dr Integration  ", email.toUpperCase(), "Doctor@123", specialtyId.longValue(), 30)
		);

		assertEquals(HttpStatus.CREATED.value(), created.statusCode());
		assertEquals("Dr Integration", JsonPath.read(created.body(), "$.data.fullName"));
		assertEquals(email, JsonPath.read(created.body(), "$.data.email"));
		assertEquals("ACTIVE", JsonPath.read(created.body(), "$.data.status"));
		Number doctorId = JsonPath.read(created.body(), "$.data.id");

		ApiHttpResponse doctorLogin = loginResponse(email, "Doctor@123");
		assertEquals(HttpStatus.OK.value(), doctorLogin.statusCode());
		assertEquals("DOCTOR", JsonPath.read(doctorLogin.body(), "$.data.user.role"));

		ApiHttpResponse updated = exchange(
				HttpMethod.PUT,
				"/api/v1/admin/doctors/" + doctorId.longValue(),
				adminToken,
				doctorJson("Dr Updated", email, null, specialtyId.longValue(), 45)
		);

		assertEquals(HttpStatus.OK.value(), updated.statusCode());
		assertEquals("Dr Updated", JsonPath.read(updated.body(), "$.data.fullName"));
		Number consultationDuration = JsonPath.read(updated.body(), "$.data.consultationDurationMinutes");
		assertEquals(45, consultationDuration.intValue());

		ApiHttpResponse list = exchange(HttpMethod.GET, "/api/v1/admin/doctors", adminToken, null);
		List<String> emails = JsonPath.read(list.body(), "$.data[*].email");

		assertEquals(HttpStatus.OK.value(), list.statusCode());
		assertTrue(emails.contains(email));

		ApiHttpResponse deactivated = exchange(
				HttpMethod.PATCH,
				"/api/v1/admin/doctors/" + doctorId.longValue() + "/deactivate",
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), deactivated.statusCode());
		assertEquals("INACTIVE", JsonPath.read(deactivated.body(), "$.data.status"));
		assertEquals(HttpStatus.FORBIDDEN.value(), loginResponse(email, "Doctor@123").statusCode());

		ApiHttpResponse activated = exchange(
				HttpMethod.PATCH,
				"/api/v1/admin/doctors/" + doctorId.longValue() + "/activate",
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), activated.statusCode());
		assertEquals("ACTIVE", JsonPath.read(activated.body(), "$.data.status"));
		assertEquals(HttpStatus.OK.value(), loginResponse(email, "Doctor@123").statusCode());
	}

	@Test
	void nonAdminAndAnonymousUsersCannotCreateDoctors() throws Exception {
		String patientToken = registerAndLoginPatient();
		Number specialtyId = firstActiveSpecialtyId();

		ApiHttpResponse anonymousCreate = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				null,
				doctorJson("Anonymous Doctor", "anonymous." + UUID.randomUUID() + "@example.com", "Doctor@123", specialtyId.longValue(), 30)
		);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), anonymousCreate.statusCode());

		ApiHttpResponse patientCreate = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				patientToken,
				doctorJson("Patient Doctor", "patient-doctor." + UUID.randomUUID() + "@example.com", "Doctor@123", specialtyId.longValue(), 30)
		);

		assertEquals(HttpStatus.FORBIDDEN.value(), patientCreate.statusCode());
	}

	@Test
	void createDoctorValidatesRequiredPasswordAndDuplicateEmail() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");
		Number specialtyId = firstActiveSpecialtyId();
		String email = "duplicate-doctor." + UUID.randomUUID() + "@example.com";

		ApiHttpResponse missingPassword = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				adminToken,
				doctorJson("No Password", "no-password." + UUID.randomUUID() + "@example.com", null, specialtyId.longValue(), 30)
		);

		assertEquals(HttpStatus.BAD_REQUEST.value(), missingPassword.statusCode());
		assertEquals("INVALID_DOCTOR_REQUEST", JsonPath.read(missingPassword.body(), "$.error.code"));

		ApiHttpResponse firstCreate = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				adminToken,
				doctorJson("First Doctor", email, "Doctor@123", specialtyId.longValue(), 30)
		);
		ApiHttpResponse duplicateCreate = exchange(
				HttpMethod.POST,
				"/api/v1/admin/doctors",
				adminToken,
				doctorJson("Duplicate Doctor", email.toUpperCase(), "Doctor@123", specialtyId.longValue(), 30)
		);

		assertEquals(HttpStatus.CREATED.value(), firstCreate.statusCode());
		assertEquals(HttpStatus.CONFLICT.value(), duplicateCreate.statusCode());
		assertEquals("DUPLICATE_EMAIL", JsonPath.read(duplicateCreate.body(), "$.error.code"));
	}

	private Number firstActiveSpecialtyId() throws Exception {
		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/specialties", null, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		return JsonPath.read(response.body(), "$.data[0].id");
	}

	private String registerAndLoginPatient() throws Exception {
		String email = "patient." + UUID.randomUUID() + "@example.com";
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
		ApiHttpResponse response = loginResponse(email, password);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		return JsonPath.read(response.body(), "$.data.accessToken");
	}

	private ApiHttpResponse loginResponse(String email, String password) throws Exception {
		return exchange(
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

	private String doctorJson(String name, String email, String password, long specialtyId, int duration) {
		if (password == null) {
			return """
					{
					  "fullName": "%s",
					  "email": "%s",
					  "phoneNumber": "+15551234567",
					  "specialtyId": %d,
					  "biography": "Clinical bio",
					  "consultationDurationMinutes": %d,
					  "clinicAddress": "100 Care Street"
					}
					""".formatted(name, email, specialtyId, duration);
		}

		return """
				{
				  "fullName": "%s",
				  "email": "%s",
				  "password": "%s",
				  "phoneNumber": "+15551234567",
				  "specialtyId": %d,
				  "biography": "Clinical bio",
				  "consultationDurationMinutes": %d,
				  "clinicAddress": "100 Care Street"
				}
				""".formatted(name, email, password, specialtyId, duration);
	}

	private record ApiHttpResponse(int statusCode, String body) {
	}
}
