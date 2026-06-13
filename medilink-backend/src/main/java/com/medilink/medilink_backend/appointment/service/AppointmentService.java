package com.medilink.medilink_backend.appointment.service;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.web.AppointmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final DoctorRefRepository doctorRefRepository;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			DoctorRefRepository doctorRefRepository
	) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRefRepository = doctorRefRepository;
	}

	public DoctorRef resolveDoctor(Long userId) {
		DoctorRef doctor = doctorRefRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorRefNotFoundException(userId));

		if (!doctor.isActive()) {
			throw new DoctorRefNotFoundException(userId);
		}

		return doctor;
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listAppointments(Long doctorId) {
		return appointmentRepository.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listAppointmentsByStatus(Long doctorId, AppointmentStatus status) {
		return appointmentRepository.findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc(doctorId, status)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public AppointmentResponse getAppointment(Long doctorId, Long appointmentId) {
		Appointment appointment = appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
		return toResponse(appointment);
	}

	@Transactional
	public AppointmentResponse updateNotes(Long doctorId, Long appointmentId, String notes) {
		Appointment appointment = appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

		appointment.updateNotes(notes);
		return toResponse(appointment);
	}

	@Transactional
	public AppointmentResponse updateStatus(Long doctorId, Long appointmentId, AppointmentStatus newStatus) {
		Appointment appointment = appointmentRepository.findByIdAndDoctorId(appointmentId, doctorId)
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

		if (!appointment.getStatus().canTransitionTo(newStatus)) {
			throw new InvalidAppointmentStatusException(
					"Cannot transition appointment " + appointmentId + " from " +
					appointment.getStatus() + " to " + newStatus
			);
		}

		appointment.updateStatus(newStatus);
		return toResponse(appointment);
	}

	private AppointmentResponse toResponse(Appointment appointment) {
		return new AppointmentResponse(
				appointment.getId(),
				appointment.getDoctorId(),
				appointment.getPatientId(),
				appointment.getAppointmentDate(),
				appointment.getStartTime(),
				appointment.getEndTime(),
				appointment.getStatus(),
				appointment.getReason(),
				appointment.getDoctorNotes()
		);
	}
}
