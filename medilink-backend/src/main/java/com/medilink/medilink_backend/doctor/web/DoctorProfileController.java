package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorProfileService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/doctor/profile")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorProfileController {

	private final DoctorProfileService doctorProfileService;

	public DoctorProfileController(DoctorProfileService doctorProfileService) {
		this.doctorProfileService = doctorProfileService;
	}

	@GetMapping
	public ApiResponse<DoctorProfileResponse> getProfile(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		return ApiResponse.success(doctorProfileService.getProfile(userId));
	}

	@PatchMapping
	public ApiResponse<DoctorProfileResponse> updateProfile(
			JwtAuthenticationToken jwt,
			@Valid @RequestBody DoctorProfileUpdateRequest request
	) {
		Long userId = jwt.getToken().getClaim("userId");
		return ApiResponse.success(doctorProfileService.updateProfile(userId, request));
	}
}
