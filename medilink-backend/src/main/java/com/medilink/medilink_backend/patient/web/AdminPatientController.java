package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.service.PatientManagementService;
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
@RequestMapping("/v1/admin/patients")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPatientController {

	private final PatientManagementService patientManagementService;

	public AdminPatientController(PatientManagementService patientManagementService) {
		this.patientManagementService = patientManagementService;
	}

	@GetMapping
	public ApiResponse<List<AdminPatientResponse>> listPatients() {
		return ApiResponse.success(patientManagementService.listPatients());
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<AdminPatientResponse> createPatient(@Valid @RequestBody AdminPatientCreateRequest request) {
		return ApiResponse.success(patientManagementService.createPatient(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<AdminPatientResponse> updatePatient(
			@PathVariable Long id,
			@Valid @RequestBody AdminPatientUpdateRequest request
	) {
		return ApiResponse.success(patientManagementService.updatePatient(id, request));
	}

	@PatchMapping("/{id}/activate")
	public ApiResponse<AdminPatientResponse> activatePatient(@PathVariable Long id) {
		return ApiResponse.success(patientManagementService.activatePatient(id));
	}

	@PatchMapping("/{id}/deactivate")
	public ApiResponse<AdminPatientResponse> deactivatePatient(@PathVariable Long id) {
		return ApiResponse.success(patientManagementService.deactivatePatient(id));
	}

	@PatchMapping("/{id}/password")
	public ApiResponse<AdminPatientResponse> resetPassword(
			@PathVariable Long id,
			@Valid @RequestBody AdminPatientPasswordRequest request
	) {
		return ApiResponse.success(patientManagementService.resetPassword(id, request));
	}
}
