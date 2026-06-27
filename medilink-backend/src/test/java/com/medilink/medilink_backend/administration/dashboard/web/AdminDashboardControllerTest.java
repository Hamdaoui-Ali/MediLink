package com.medilink.medilink_backend.administration.dashboard.web;

import com.medilink.medilink_backend.administration.dashboard.service.AdminDashboardService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDashboardControllerTest {

	private final AdminDashboardService adminDashboardService = mock(AdminDashboardService.class);
	private final AdminDashboardController controller = new AdminDashboardController(adminDashboardService);

	@Test
	void getOverviewReturnsApiResponseWithDashboardData() {
		DashboardOverviewResponse overview = new DashboardOverviewResponse(
				3L, 10L, 5L, 4L, Collections.emptyList()
		);
		when(adminDashboardService.getOverview()).thenReturn(overview);

		ApiResponse<DashboardOverviewResponse> response = controller.getOverview();

		assertTrue(response.success());
		assertNotNull(response.data());
		assertEquals(3L, response.data().totalDoctors());
		assertEquals(10L, response.data().totalPatients());
		assertEquals(5L, response.data().totalAppointments());
		assertEquals(4L, response.data().totalSpecialties());
	}

	@Test
	void getOverviewIncludesRecentAppointmentsInWrappedResponse() {
		DashboardOverviewResponse.RecentAppointment appointment = new DashboardOverviewResponse.RecentAppointment(
				1L, LocalDate.of(2026, 6, 15), LocalTime.of(10, 0),
				"Dr. Smith", "Jane Patient", "CONFIRMED"
		);

		DashboardOverviewResponse overview = new DashboardOverviewResponse(
				1L, 1L, 1L, 1L, java.util.List.of(appointment)
		);
		when(adminDashboardService.getOverview()).thenReturn(overview);

		ApiResponse<DashboardOverviewResponse> response = controller.getOverview();

		assertEquals(1, response.data().recentAppointments().size());
		DashboardOverviewResponse.RecentAppointment recent = response.data().recentAppointments().getFirst();
		assertEquals("Dr. Smith", recent.doctorName());
		assertEquals("Jane Patient", recent.patientName());
		assertEquals("CONFIRMED", recent.status());
	}

	@Test
	void dashboardEndpointRequiresAdminRole() throws NoSuchMethodException {
		Method method = AdminDashboardController.class.getMethod("getOverview");
		PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

		assertNotNull(preAuthorize);
		assertEquals("hasRole('ADMIN')", preAuthorize.value());
	}
}
