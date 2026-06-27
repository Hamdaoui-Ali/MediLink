package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorAvailabilityService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping("/v1/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAvailabilityController {

	private final DoctorAvailabilityService availabilityService;

	public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@GetMapping("/availability")
	public ApiResponse<List<DoctorAvailabilityResponse>> listAvailability(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		Long doctorId = availabilityService.resolveDoctor(userId).getId();
		return ApiResponse.success(availabilityService.listAvailability(doctorId));
	}

	@PostMapping("/availability")
	public ResponseEntity<ApiResponse<DoctorAvailabilityResponse>> addAvailability(
			JwtAuthenticationToken jwt,
			@Valid @RequestBody DoctorAvailabilityRequest request
	) {
		Long userId = jwt.getToken().getClaim("userId");
		Long doctorId = availabilityService.resolveDoctor(userId).getId();
		DoctorAvailabilityResponse response = availabilityService.addAvailability(doctorId, request);
		return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
	}

	@PutMapping("/availability/{id}")
	public ApiResponse<DoctorAvailabilityResponse> updateAvailability(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@Valid @RequestBody DoctorAvailabilityRequest request
	) {
		Long userId = jwt.getToken().getClaim("userId");
		Long doctorId = availabilityService.resolveDoctor(userId).getId();
		return ApiResponse.success(availabilityService.updateAvailability(doctorId, id, request));
	}

	@DeleteMapping("/availability/{id}")
	public ApiResponse<DoctorAvailabilityResponse> deactivateAvailability(
			JwtAuthenticationToken jwt,
			@PathVariable Long id
	) {
		Long userId = jwt.getToken().getClaim("userId");
		Long doctorId = availabilityService.resolveDoctor(userId).getId();
		return ApiResponse.success(availabilityService.deactivateAvailability(doctorId, id));
	}
}
