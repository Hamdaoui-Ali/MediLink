package com.medilink.medilink_backend.administration.dashboard.web;

import com.medilink.medilink_backend.administration.dashboard.service.AdminDashboardService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/admin")
public class AdminDashboardController {

	private final AdminDashboardService adminDashboardService;

	public AdminDashboardController(AdminDashboardService adminDashboardService) {
		this.adminDashboardService = adminDashboardService;
	}

	@GetMapping("/dashboard")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<DashboardOverviewResponse> getOverview() {
		return ApiResponse.success(adminDashboardService.getOverview());
	}
}
