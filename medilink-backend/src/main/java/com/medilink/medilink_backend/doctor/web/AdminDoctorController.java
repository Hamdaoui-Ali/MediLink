package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorManagementService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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
	public ApiResponse<List<AdminDoctorResponse>> list() {
		return ApiResponse.success(doctorManagementService.list());
	}

	@PostMapping
	public ResponseEntity<ApiResponse<AdminDoctorResponse>> create(@Valid @RequestBody AdminDoctorRequest request) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(doctorManagementService.create(request)));
	}

	@PutMapping("/{id}")
	public ApiResponse<AdminDoctorResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody AdminDoctorRequest request
	) {
		return ApiResponse.success(doctorManagementService.update(id, request));
	}

	@PatchMapping("/{id}/activate")
	public ApiResponse<AdminDoctorResponse> activate(@PathVariable Long id) {
		return ApiResponse.success(doctorManagementService.activate(id));
	}

	@PatchMapping("/{id}/deactivate")
	public ApiResponse<AdminDoctorResponse> deactivate(@PathVariable Long id) {
		return ApiResponse.success(doctorManagementService.deactivate(id));
	}
}
