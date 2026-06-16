package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.patient.service.PatientRegistrationService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PatientRegistrationControllerTest {

	@Test
	void registerReturnsCreatedApiResponse() {
		PatientRegistrationService service = mock(PatientRegistrationService.class);
		PatientRegistrationController controller = new PatientRegistrationController(service);
		PatientRegistrationRequest request = new PatientRegistrationRequest(
				"Jane Patient",
				"jane@example.com",
				"Patient@123",
				"+15551234567",
				null,
				null,
				null
		);
		PatientRegistrationResponse registration = new PatientRegistrationResponse(
				1L,
				2L,
				"Jane Patient",
				"jane@example.com",
				"+15551234567",
				RoleName.PATIENT,
				AccountStatus.ACTIVE
		);
		when(service.register(request)).thenReturn(registration);

		ResponseEntity<ApiResponse<PatientRegistrationResponse>> response = controller.register(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().success());
		assertEquals(registration, response.getBody().data());
	}
}
