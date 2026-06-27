package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.domain.Gender;
import com.medilink.medilink_backend.patient.service.PatientManagementService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminPatientControllerTest {

	private final PatientManagementService patientManagementService = mock(PatientManagementService.class);
	private final AdminPatientController controller = new AdminPatientController(patientManagementService);

	@Test
	void listPatientsReturnsServiceResponse() {
		List<AdminPatientResponse> patients = List.of(response("Jane Patient"));
		when(patientManagementService.listPatients()).thenReturn(patients);

		ApiResponse<List<AdminPatientResponse>> response = controller.listPatients();

		assertTrue(response.success());
		assertEquals(patients, response.data());
	}

	@Test
	void createPatientReturnsCreatedApiResponse() {
		AdminPatientCreateRequest request = new AdminPatientCreateRequest(
				"Jane Patient",
				"jane@medilink.local",
				"Password123",
				null,
				LocalDate.of(1990, 1, 15),
				Gender.FEMALE,
				null
		);
		AdminPatientResponse patient = response("Jane Patient");
		when(patientManagementService.createPatient(request)).thenReturn(patient);

		ApiResponse<AdminPatientResponse> response = controller.createPatient(request);

		assertTrue(response.success());
		assertEquals(patient, response.data());

		Method method = method("createPatient", AdminPatientCreateRequest.class);
		assertEquals(HttpStatus.CREATED, method.getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
	}

	@Test
	void updateStatusAndPasswordEndpointsDelegateToService() {
		AdminPatientUpdateRequest request = new AdminPatientUpdateRequest(
				"Jane Patient",
				"jane@medilink.local",
				null,
				null,
				Gender.UNSPECIFIED,
				null
		);
		AdminPatientPasswordRequest passwordRequest = new AdminPatientPasswordRequest("NewPassword123");
		when(patientManagementService.updatePatient(1L, request)).thenReturn(response("Jane Patient"));
		when(patientManagementService.activatePatient(1L)).thenReturn(response("Jane Active"));
		when(patientManagementService.deactivatePatient(1L)).thenReturn(response("Jane Inactive"));
		when(patientManagementService.resetPassword(1L, passwordRequest)).thenReturn(response("Jane Reset"));

		assertEquals("Jane Patient", controller.updatePatient(1L, request).data().fullName());
		assertEquals("Jane Active", controller.activatePatient(1L).data().fullName());
		assertEquals("Jane Inactive", controller.deactivatePatient(1L).data().fullName());
		assertEquals("Jane Reset", controller.resetPassword(1L, passwordRequest).data().fullName());
	}

	@Test
	void controllerRequiresAdminRole() {
		PreAuthorize preAuthorize = AdminPatientController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("hasRole('ADMIN')", preAuthorize.value());
	}

	private Method method(String name, Class<?>... parameterTypes) {
		try {
			return AdminPatientController.class.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
	}

	private AdminPatientResponse response(String fullName) {
		return new AdminPatientResponse(
				1L,
				2L,
				fullName,
				"jane@medilink.local",
				null,
				"ACTIVE",
				LocalDate.of(1990, 1, 15),
				Gender.FEMALE,
				null
		);
	}
}
