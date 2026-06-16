package com.medilink.medilink_backend.availability.web;

import com.medilink.medilink_backend.availability.service.DoctorAvailabilityService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/v1/patient/doctors")
@PreAuthorize("hasRole('PATIENT')")
public class DoctorAvailabilityController {

	private final DoctorAvailabilityService availabilityService;

	public DoctorAvailabilityController(DoctorAvailabilityService availabilityService) {
		this.availabilityService = availabilityService;
	}

	@GetMapping("/{doctorId}/slots")
	public ApiResponse<List<SlotResponse>> getAvailableSlots(
			@PathVariable Long doctorId,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate date
	) {
		List<SlotResponse> slots = availabilityService.getAvailableSlots(doctorId, date);
		return ApiResponse.success(slots);
	}
}
