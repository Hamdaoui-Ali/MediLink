package com.medilink.medilink_backend.identity.web;

import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthControllerTest {

	@Test
	void shouldReturnSuccessfulHealthResponse() {
		HealthController controller = new HealthController();

		ApiResponse<HealthController.HealthStatusResponse> response = controller.health();

		assertTrue(response.success());
		assertNotNull(response.data());
		assertEquals("UP", response.data().status());
		assertEquals("medilink-backend", response.data().service());
	}
}
