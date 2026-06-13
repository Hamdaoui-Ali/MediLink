package com.medilink.medilink_backend.appointment.web;

import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.service.AppointmentService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/doctor/appointments")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorAppointmentController {

	private final AppointmentService appointmentService;

	public DoctorAppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@GetMapping
	public ApiResponse<List<AppointmentResponse>> listAppointments(
			JwtAuthenticationToken jwt,
			@RequestParam(required = false) AppointmentStatus status
	) {
		Long doctorId = resolveDoctorId(jwt);
		List<AppointmentResponse> appointments = status == null
				? appointmentService.listAppointments(doctorId)
				: appointmentService.listAppointmentsByStatus(doctorId, status);
		return ApiResponse.success(appointments);
	}

	@GetMapping("/{id}")
	public ApiResponse<AppointmentResponse> getAppointment(
			JwtAuthenticationToken jwt,
			@PathVariable Long id
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(appointmentService.getAppointment(doctorId, id));
	}

	@PatchMapping("/{id}/notes")
	public ApiResponse<AppointmentResponse> updateNotes(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@Valid @RequestBody UpdateNotesRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(appointmentService.updateNotes(doctorId, id, request.notes()));
	}

	@PatchMapping("/{id}/status")
	public ApiResponse<AppointmentResponse> updateStatus(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@Valid @RequestBody UpdateStatusRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(appointmentService.updateStatus(doctorId, id, request.status()));
	}

	private Long resolveDoctorId(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		return appointmentService.resolveDoctor(userId).getId();
	}
}
