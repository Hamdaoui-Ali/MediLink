package com.medilink.medilink_backend.administration.web;

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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SpecialtyManagementIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Test
	void adminCanManageSpecialtyLifecycleThroughHttpApi() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");
		String specialtyName = "Integration Cardiology " + UUID.randomUUID();
		String updatedName = specialtyName + " Updated";

		ApiHttpResponse created = exchange(
				HttpMethod.POST,
				"/api/v1/specialties",
				adminToken,
				specialtyJson("  " + specialtyName + "  ", "  Heart care  ")
		);

		assertEquals(HttpStatus.CREATED.value(), created.statusCode());
		assertEquals(specialtyName, JsonPath.read(created.body(), "$.data.name"));
		assertEquals("Heart care", JsonPath.read(created.body(), "$.data.description"));
		Number specialtyId = JsonPath.read(created.body(), "$.data.id");

		ApiHttpResponse updated = exchange(
				HttpMethod.PUT,
				"/api/v1/specialties/" + specialtyId.longValue(),
				adminToken,
				specialtyJson(updatedName, "Updated care")
		);

		assertEquals(HttpStatus.OK.value(), updated.statusCode());
		assertEquals(updatedName, JsonPath.read(updated.body(), "$.data.name"));

		ApiHttpResponse deactivated = exchange(
				HttpMethod.PATCH,
				"/api/v1/specialties/" + specialtyId.longValue() + "/deactivate",
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), deactivated.statusCode());
		assertEquals("INACTIVE", JsonPath.read(deactivated.body(), "$.data.status"));

		ApiHttpResponse publicActiveList = exchange(HttpMethod.GET, "/api/v1/specialties", null, null);
		List<String> publicNames = JsonPath.read(publicActiveList.body(), "$.data[*].name");
		assertEquals(HttpStatus.OK.value(), publicActiveList.statusCode());
		assertFalse(publicNames.contains(updatedName));

		ApiHttpResponse adminAllList = exchange(
				HttpMethod.GET,
				"/api/v1/specialties?activeOnly=false",
				adminToken,
				null
		);
		List<String> adminNames = JsonPath.read(adminAllList.body(), "$.data[*].name");
		assertEquals(HttpStatus.OK.value(), adminAllList.statusCode());
		assertTrue(adminNames.contains(updatedName));

		ApiHttpResponse activated = exchange(
				HttpMethod.PATCH,
				"/api/v1/specialties/" + specialtyId.longValue() + "/activate",
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), activated.statusCode());
		assertEquals("ACTIVE", JsonPath.read(activated.body(), "$.data.status"));

		ApiHttpResponse softDeleted = exchange(
				HttpMethod.DELETE,
				"/api/v1/specialties/" + specialtyId.longValue(),
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), softDeleted.statusCode());
		assertEquals("INACTIVE", JsonPath.read(softDeleted.body(), "$.data.status"));
	}

	@Test
	void nonAdminAndAnonymousUsersCannotModifySpecialties() throws Exception {
		String patientToken = registerAndLoginPatient();

		ApiHttpResponse anonymousCreate = exchange(
				HttpMethod.POST,
				"/api/v1/specialties",
				null,
				specialtyJson("Anonymous Specialty " + UUID.randomUUID(), null)
		);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), anonymousCreate.statusCode());

		ApiHttpResponse patientCreate = exchange(
				HttpMethod.POST,
				"/api/v1/specialties",
				patientToken,
				specialtyJson("Patient Specialty " + UUID.randomUUID(), null)
		);

		assertEquals(HttpStatus.FORBIDDEN.value(), patientCreate.statusCode());

		ApiHttpResponse anonymousAllList = exchange(HttpMethod.GET, "/api/v1/specialties?activeOnly=false", null, null);

		assertEquals(HttpStatus.FORBIDDEN.value(), anonymousAllList.statusCode());
	}

	@Test
	void duplicateSpecialtyNameReturnsConflictThroughHttpApi() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");
		String specialtyName = "Duplicate Specialty " + UUID.randomUUID();

		ApiHttpResponse firstCreate = exchange(
				HttpMethod.POST,
				"/api/v1/specialties",
				adminToken,
				specialtyJson(specialtyName, "Original")
		);
		ApiHttpResponse duplicateCreate = exchange(
				HttpMethod.POST,
				"/api/v1/specialties",
				adminToken,
				specialtyJson("  " + specialtyName.toUpperCase() + "  ", "Duplicate")
		);

		assertEquals(HttpStatus.CREATED.value(), firstCreate.statusCode());
		assertEquals(HttpStatus.CONFLICT.value(), duplicateCreate.statusCode());
		assertEquals("DUPLICATE_SPECIALTY_NAME", JsonPath.read(duplicateCreate.body(), "$.error.code"));
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

	private String specialtyJson(String name, String description) {
		if (description == null) {
			return """
					{
					  "name": "%s"
					}
					""".formatted(name);
		}

		return """
				{
				  "name": "%s",
				  "description": "%s"
				}
				""".formatted(name, description);
	}

	private record ApiHttpResponse(int statusCode, String body) {
	}
}
