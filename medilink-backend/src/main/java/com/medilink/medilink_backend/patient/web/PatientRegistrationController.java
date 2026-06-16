package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.patient.service.PatientRegistrationService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/patients")
public class PatientRegistrationController {

	private final PatientRegistrationService patientRegistrationService;

	public PatientRegistrationController(PatientRegistrationService patientRegistrationService) {
		this.patientRegistrationService = patientRegistrationService;
	}

	@PostMapping("/register")
	public ResponseEntity<ApiResponse<PatientRegistrationResponse>> register(
			@Valid @RequestBody PatientRegistrationRequest request
	) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(patientRegistrationService.register(request)));
	}
}
