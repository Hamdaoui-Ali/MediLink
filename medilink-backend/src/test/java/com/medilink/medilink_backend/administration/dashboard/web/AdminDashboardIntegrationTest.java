package com.medilink.medilink_backend.administration.dashboard.web;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminDashboardIntegrationTest {

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@Value("${local.server.port}")
	private int port;

	@Test
	void adminCanRetrieveDashboardOverviewThroughHttpApi() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");

		ApiHttpResponse response = exchange(
				HttpMethod.GET,
				"/api/v1/admin/dashboard",
				adminToken,
				null
		);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertEquals(true, JsonPath.read(response.body(), "$.success"));
		assertNotNull(JsonPath.read(response.body(), "$.data.totalDoctors"));
		assertNotNull(JsonPath.read(response.body(), "$.data.totalPatients"));
		assertNotNull(JsonPath.read(response.body(), "$.data.totalAppointments"));
		assertNotNull(JsonPath.read(response.body(), "$.data.totalSpecialties"));
		assertNotNull(JsonPath.read(response.body(), "$.data.recentAppointments"));
	}

	@Test
	void nonAdminUsersCannotAccessDashboard() throws Exception {
		String patientToken = registerAndLoginPatient();

		ApiHttpResponse patientResponse = exchange(
				HttpMethod.GET,
				"/api/v1/admin/dashboard",
				patientToken,
				null
		);

		assertEquals(HttpStatus.FORBIDDEN.value(), patientResponse.statusCode());
	}

	@Test
	void unauthenticatedUsersCannotAccessDashboard() throws Exception {
		ApiHttpResponse anonymousResponse = exchange(
				HttpMethod.GET,
				"/api/v1/admin/dashboard",
				null,
				null
		);

		assertEquals(HttpStatus.UNAUTHORIZED.value(), anonymousResponse.statusCode());
	}

	@Test
	void dashboardDataIsConsistentAcrossMultipleRequests() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");

		ApiHttpResponse first = exchange(HttpMethod.GET, "/api/v1/admin/dashboard", adminToken, null);
		ApiHttpResponse second = exchange(HttpMethod.GET, "/api/v1/admin/dashboard", adminToken, null);

		assertEquals(HttpStatus.OK.value(), first.statusCode());
		assertEquals(HttpStatus.OK.value(), second.statusCode());
		Object firstCount = JsonPath.read(first.body(), "$.data.totalSpecialties");
		Object secondCount = JsonPath.read(second.body(), "$.data.totalSpecialties");
		assertEquals(firstCount, secondCount);
	}

	@Test
	void dashboardIncludesRecentAppointmentsWhenPresent() throws Exception {
		String adminToken = login("admin@medilink.local", "Admin@12345");

		ApiHttpResponse response = exchange(HttpMethod.GET, "/api/v1/admin/dashboard", adminToken, null);

		assertEquals(HttpStatus.OK.value(), response.statusCode());
		assertTrue(response.body().contains("recentAppointments"));
	}

	private String registerAndLoginPatient() throws Exception {
		String email = "patient." + java.util.UUID.randomUUID() + "@example.com";
		ApiHttpResponse response = exchange(
				HttpMethod.POST,
				"/api/v1/patients/register",
				null,
				"""
						{
						  "fullName": "Test Patient",
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
