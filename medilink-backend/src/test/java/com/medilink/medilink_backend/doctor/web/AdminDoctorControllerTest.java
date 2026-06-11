package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.domain.DoctorStatus;
import com.medilink.medilink_backend.doctor.service.DoctorManagementService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDoctorControllerTest {

	private final DoctorManagementService doctorManagementService = mock(DoctorManagementService.class);
	private final AdminDoctorController adminDoctorController = new AdminDoctorController(doctorManagementService);

	@Test
	void listReturnsDoctorsFromService() {
		AdminDoctorResponse doctor = doctorResponse();
		when(doctorManagementService.list()).thenReturn(List.of(doctor));

		ApiResponse<List<AdminDoctorResponse>> response = adminDoctorController.list();

		assertTrue(response.success());
		assertEquals(List.of(doctor), response.data());
	}

	@Test
	void createReturnsCreatedApiResponse() {
		AdminDoctorRequest request = doctorRequest("Doctor@123");
		AdminDoctorResponse doctor = doctorResponse();
		when(doctorManagementService.create(request)).thenReturn(doctor);

		ResponseEntity<ApiResponse<AdminDoctorResponse>> response = adminDoctorController.create(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().success());
		assertEquals(doctor, response.getBody().data());
	}

	@Test
	void updateAndStatusActionsReturnSuccessfulApiResponses() {
		AdminDoctorRequest request = doctorRequest(null);
		AdminDoctorResponse doctor = doctorResponse();
		when(doctorManagementService.update(1L, request)).thenReturn(doctor);
		when(doctorManagementService.activate(1L)).thenReturn(doctor);
		when(doctorManagementService.deactivate(1L)).thenReturn(doctor);

		assertEquals(doctor, adminDoctorController.update(1L, request).data());
		assertEquals(doctor, adminDoctorController.activate(1L).data());
		assertEquals(doctor, adminDoctorController.deactivate(1L).data());
	}

	@Test
	void controllerRequiresAdminRole() {
		PreAuthorize preAuthorize = AdminDoctorController.class.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("hasRole('ADMIN')", preAuthorize.value());
	}

	private AdminDoctorRequest doctorRequest(String password) {
		return new AdminDoctorRequest(
				"Dr Jane",
				"doctor@example.com",
				password,
				"+1555",
				1L,
				"Bio",
				30,
				"Clinic"
		);
	}

	private AdminDoctorResponse doctorResponse() {
		return new AdminDoctorResponse(
				1L,
				2L,
				"Dr Jane",
				"doctor@example.com",
				"+1555",
				3L,
				"Cardiology",
				"Bio",
				30,
				"Clinic",
				DoctorStatus.ACTIVE
		);
	}
}
