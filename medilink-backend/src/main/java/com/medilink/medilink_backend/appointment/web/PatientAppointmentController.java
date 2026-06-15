package com.medilink.medilink_backend.appointment.web;

import com.medilink.medilink_backend.appointment.service.AppointmentService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/v1/patient/appointments")
@PreAuthorize("hasRole('PATIENT')")
public class PatientAppointmentController {

	private final AppointmentService appointmentService;

	public PatientAppointmentController(AppointmentService appointmentService) {
		this.appointmentService = appointmentService;
	}

	@PostMapping
	public ApiResponse<AppointmentResponse> bookAppointment(
			JwtAuthenticationToken jwt,
			@Valid @RequestBody BookAppointmentRequest request
	) {
		Long patientId = resolvePatientId(jwt);
		AppointmentResponse appointment = appointmentService.createAppointment(
				patientId, request.doctorId(), request.appointmentDate(),
				request.startTime(), request.reason());
		return ApiResponse.success(appointment);
	}

	@GetMapping
	public ApiResponse<List<AppointmentResponse>> listAppointments(
			JwtAuthenticationToken jwt
	) {
		Long patientId = resolvePatientId(jwt);
		List<AppointmentResponse> appointments = appointmentService.listPatientAppointments(patientId);
		return ApiResponse.success(appointments);
	}

	@GetMapping("/{id}")
	public ApiResponse<AppointmentResponse> getAppointment(
			JwtAuthenticationToken jwt,
			@PathVariable Long id
	) {
		Long patientId = resolvePatientId(jwt);
		return ApiResponse.success(appointmentService.getPatientAppointment(patientId, id));
	}

	@PatchMapping("/{id}/cancel")
	public ApiResponse<AppointmentResponse> cancelAppointment(
			JwtAuthenticationToken jwt,
			@PathVariable Long id
	) {
		Long patientId = resolvePatientId(jwt);
		return ApiResponse.success(appointmentService.cancelAppointment(patientId, id));
	}

	@PatchMapping("/{id}/reschedule")
	public ApiResponse<AppointmentResponse> rescheduleAppointment(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = ISO.DATE) LocalDate newDate,
			@RequestParam @DateTimeFormat(iso = ISO.TIME) LocalTime newStartTime
	) {
		Long patientId = resolvePatientId(jwt);
		return ApiResponse.success(appointmentService.rescheduleAppointment(
				patientId, id, newDate, newStartTime));
	}

	private Long resolvePatientId(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		return appointmentService.resolvePatient(userId).getId();
	}
}
