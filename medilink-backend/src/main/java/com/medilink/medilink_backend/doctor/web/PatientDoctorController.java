package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.service.DoctorSearchService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/patient/doctors")
@PreAuthorize("hasRole('PATIENT')")
public class PatientDoctorController {

	private final DoctorSearchService doctorSearchService;
	private final DoctorSearchMapper doctorSearchMapper;

	public PatientDoctorController(DoctorSearchService doctorSearchService, DoctorSearchMapper doctorSearchMapper) {
		this.doctorSearchService = doctorSearchService;
		this.doctorSearchMapper = doctorSearchMapper;
	}

	@GetMapping
	public ApiResponse<List<DoctorSummaryResponse>> searchDoctors(
			@RequestParam(required = false) Long specialtyId,
			@RequestParam(required = false) String name
	) {
		var doctors = doctorSearchService.searchDoctors(specialtyId, name);
		return ApiResponse.success(doctorSearchMapper.toSummaryList(doctors));
	}

	@GetMapping("/{id}")
	public ApiResponse<DoctorProfileResponse> getDoctor(@PathVariable Long id) {
		var doctor = doctorSearchService.getDoctor(id);
		return ApiResponse.success(doctorSearchMapper.toProfile(doctor));
	}
}
