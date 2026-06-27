package com.medilink.medilink_backend.availability.web;

import com.medilink.medilink_backend.availability.service.DoctorAvailabilityService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/doctor/availability")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAvailabilityManagementController {

	private final DoctorAvailabilityService availabilityService;

	public DoctorAvailabilityManagementController(DoctorAvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@GetMapping
	public ApiResponse<List<AvailabilityResponse>> listAvailability(JwtAuthenticationToken jwt) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(availabilityService.listAvailability(doctorId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AvailabilityResponse> createAvailability(
			JwtAuthenticationToken jwt,
			@Valid @RequestBody AvailabilityRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(availabilityService.createAvailability(doctorId, request));
	}

	@PatchMapping("/{id}")
	public ApiResponse<AvailabilityResponse> updateAvailability(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@Valid @RequestBody AvailabilityRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(availabilityService.updateAvailability(doctorId, id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteAvailability(JwtAuthenticationToken jwt, @PathVariable Long id) {
		Long doctorId = resolveDoctorId(jwt);
		availabilityService.deleteAvailability(doctorId, id);
		return ApiResponse.success(null);
	}

	private Long resolveDoctorId(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		return availabilityService.resolveDoctor(userId).getId();
	}
}
