package com.medilink.medilink_backend.administration.web;

import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpecialtyControllerTest {

	private final SpecialtyService specialtyService = mock(SpecialtyService.class);
	private final SpecialtyController specialtyController = new SpecialtyController(specialtyService);

	@Test
	void listReturnsSpecialtiesFromService() {
		List<SpecialtyResponse> specialties = List.of(new SpecialtyResponse(
				1L,
				"Cardiology",
				"Heart care",
				SpecialtyStatus.ACTIVE
		));
		when(specialtyService.list(true)).thenReturn(specialties);

		ApiResponse<List<SpecialtyResponse>> response = specialtyController.list(true);

		assertTrue(response.success());
		assertEquals(specialties, response.data());
	}

	@Test
	void createReturnsCreatedApiResponse() {
		SpecialtyRequest request = new SpecialtyRequest("Cardiology", "Heart care");
		SpecialtyResponse specialty = new SpecialtyResponse(1L, "Cardiology", "Heart care", SpecialtyStatus.ACTIVE);
		when(specialtyService.create(request)).thenReturn(specialty);

		ResponseEntity<ApiResponse<SpecialtyResponse>> response = specialtyController.create(request);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertTrue(response.getBody().success());
		assertEquals(specialty, response.getBody().data());
	}

	@Test
	void modifyingEndpointsRequireAdminRole() throws NoSuchMethodException {
		assertAdminOnly("get", Long.class);
		assertAdminOnly("create", SpecialtyRequest.class);
		assertAdminOnly("update", Long.class, SpecialtyRequest.class);
		assertAdminOnly("activate", Long.class);
		assertAdminOnly("deactivate", Long.class);
		assertAdminOnly("delete", Long.class);
	}

	@Test
	void listAllowsPublicActiveOnlyRequestsAndRequiresAdminForAllStatuses() throws NoSuchMethodException {
		Method method = SpecialtyController.class.getMethod("list", boolean.class);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("#activeOnly or hasRole('ADMIN')", preAuthorize.value());
	}

	private void assertAdminOnly(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
		Method method = SpecialtyController.class.getMethod(methodName, parameterTypes);
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("hasRole('ADMIN')", preAuthorize.value());
	}
}
