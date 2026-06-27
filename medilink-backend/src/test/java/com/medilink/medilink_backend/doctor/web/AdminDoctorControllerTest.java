package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorManagementService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDoctorControllerTest {

	private final DoctorManagementService doctorManagementService = mock(DoctorManagementService.class);
	private final AdminDoctorController controller = new AdminDoctorController(doctorManagementService);

	@Test
	void listDoctorsReturnsServiceResponse() {
		List<AdminDoctorResponse> doctors = List.of(response("Dr. Jane"));
		when(doctorManagementService.listDoctors()).thenReturn(doctors);

		ApiResponse<List<AdminDoctorResponse>> response = controller.listDoctors();

		assertTrue(response.success());
		assertEquals(doctors, response.data());
	}

	@Test
	void createDoctorReturnsCreatedApiResponse() {
		AdminDoctorCreateRequest request = new AdminDoctorCreateRequest(
				"Dr. Jane",
				"jane@medilink.local",
				"Password123",
				null,
				1L,
				null,
				30,
				null
		);
		AdminDoctorResponse doctor = response("Dr. Jane");
		when(doctorManagementService.createDoctor(request)).thenReturn(doctor);

		ApiResponse<AdminDoctorResponse> response = controller.createDoctor(request);

		assertTrue(response.success());
		assertEquals(doctor, response.data());

		Method method = method("createDoctor", AdminDoctorCreateRequest.class);
		assertEquals(HttpStatus.CREATED, method.getAnnotation(org.springframework.web.bind.annotation.ResponseStatus.class).value());
	}

	@Test
	void updateAndStatusEndpointsDelegateToService() {
		AdminDoctorUpdateRequest request = new AdminDoctorUpdateRequest(
				"Dr. Jane",
				"jane@medilink.local",
				null,
				1L,
				null,
				30,
				null
		);
		when(doctorManagementService.updateDoctor(1L, request)).thenReturn(response("Dr. Jane"));
		when(doctorManagementService.activateDoctor(1L)).thenReturn(response("Dr. Active"));
		when(doctorManagementService.deactivateDoctor(1L)).thenReturn(response("Dr. Inactive"));
		AdminDoctorPasswordRequest passwordRequest = new AdminDoctorPasswordRequest("NewPassword123");
		when(doctorManagementService.resetPassword(1L, passwordRequest)).thenReturn(response("Dr. Reset"));

		assertEquals("Dr. Jane", controller.updateDoctor(1L, request).data().fullName());
		assertEquals("Dr. Active", controller.activateDoctor(1L).data().fullName());
		assertEquals("Dr. Inactive", controller.deactivateDoctor(1L).data().fullName());
		assertEquals("Dr. Reset", controller.resetPassword(1L, passwordRequest).data().fullName());
	}

	@Test
	void controllerRequiresAdminRole() {
		PreAuthorize preAuthorize = AdminDoctorController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("hasRole('ADMIN')", preAuthorize.value());
	}

	private Method method(String name, Class<?>... parameterTypes) {
		try {
			return AdminDoctorController.class.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException exception) {
			throw new AssertionError(exception);
		}
	}

	private AdminDoctorResponse response(String fullName) {
		return new AdminDoctorResponse(
				1L,
				2L,
				fullName,
				"jane@medilink.local",
				null,
				"ACTIVE",
				3L,
				"Cardiology",
				null,
				30,
				null,
				"ACTIVE"
		);
	}
}
