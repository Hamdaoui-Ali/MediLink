package com.medilink.medilink_backend.identity.web;

import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/v1/health")
public class HealthController {

	@GetMapping
	public ApiResponse<HealthStatusResponse> health() {
		return ApiResponse.success(new HealthStatusResponse("UP", "medilink-backend", Instant.now()));
	}

	public record HealthStatusResponse(String status, String service, Instant checkedAt) {
	}
}
