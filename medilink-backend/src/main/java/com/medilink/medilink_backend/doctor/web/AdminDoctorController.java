package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorManagementService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/doctors")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDoctorController {

	private final DoctorManagementService doctorManagementService;

	public AdminDoctorController(DoctorManagementService doctorManagementService) {
		this.doctorManagementService = doctorManagementService;
	}

	@GetMapping
	public ApiResponse<List<AdminDoctorResponse>> listDoctors() {
		return ApiResponse.success(doctorManagementService.listDoctors());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AdminDoctorResponse> createDoctor(@Valid @RequestBody AdminDoctorCreateRequest request) {
		return ApiResponse.success(doctorManagementService.createDoctor(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<AdminDoctorResponse> updateDoctor(
			@PathVariable Long id,
			@Valid @RequestBody AdminDoctorUpdateRequest request
	) {
		return ApiResponse.success(doctorManagementService.updateDoctor(id, request));
	}

	@PatchMapping("/{id}/activate")
	public ApiResponse<AdminDoctorResponse> activateDoctor(@PathVariable Long id) {
		return ApiResponse.success(doctorManagementService.activateDoctor(id));
	}

	@PatchMapping("/{id}/deactivate")
	public ApiResponse<AdminDoctorResponse> deactivateDoctor(@PathVariable Long id) {
		return ApiResponse.success(doctorManagementService.deactivateDoctor(id));
	}

	@PatchMapping("/{id}/password")
	public ApiResponse<AdminDoctorResponse> resetPassword(
			@PathVariable Long id,
			@Valid @RequestBody AdminDoctorPasswordRequest request
	) {
		return ApiResponse.success(doctorManagementService.resetPassword(id, request));
	}
}
