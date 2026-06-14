package com.medilink.medilink_backend.appointment.service;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.domain.PatientRef;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.web.AppointmentResponse;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final DoctorRefRepository doctorRefRepository;
	private final PatientRepository patientRepository;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			DoctorRefRepository doctorRefRepository,
			PatientRepository patientRepository
	) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRefRepository = doctorRefRepository;
		this.patientRepository = patientRepository;
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
		List<Appointment> appointments = appointmentRepository
				.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId);
		return toResponses(appointments);
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listAppointmentsByStatus(Long doctorId, AppointmentStatus status) {
		List<Appointment> appointments = appointmentRepository
				.findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc(doctorId, status);
		return toResponses(appointments);
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listFilteredAppointments(
			Long doctorId, AppointmentStatus status, LocalDate from, LocalDate to
	) {
		List<Appointment> appointments;
		boolean hasStatus = status != null;
		boolean hasDateRange = from != null && to != null;

		if (hasStatus && hasDateRange) {
			appointments = appointmentRepository
					.findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
							doctorId, status, from, to);
		} else if (hasStatus) {
			appointments = appointmentRepository
					.findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc(doctorId, status);
		} else if (hasDateRange) {
			appointments = appointmentRepository
					.findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
							doctorId, from, to);
		} else {
			appointments = appointmentRepository
					.findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(doctorId);
		}

		return toResponses(appointments);
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

	private List<AppointmentResponse> toResponses(List<Appointment> appointments) {
		if (appointments.isEmpty()) {
			return List.of();
		}

		List<Long> patientIds = appointments.stream()
				.map(Appointment::getPatientId)
				.distinct()
				.toList();

		Map<Long, String> patientNames = patientRepository.findPatientNamesByIds(patientIds)
				.stream()
				.collect(Collectors.toMap(PatientRef::id, PatientRef::fullName));

		return appointments.stream()
				.map(appointment -> toResponse(appointment, patientNames.getOrDefault(appointment.getPatientId(), "Unknown")))
				.toList();
	}

	private AppointmentResponse toResponse(Appointment appointment) {
		return toResponse(appointment, "Unknown");
	}

	private AppointmentResponse toResponse(Appointment appointment, String patientName) {
		return new AppointmentResponse(
				appointment.getId(),
				appointment.getDoctorId(),
				appointment.getPatientId(),
				patientName,
				appointment.getAppointmentDate(),
				appointment.getStartTime(),
				appointment.getEndTime(),
				appointment.getStatus(),
				appointment.getReason(),
				appointment.getDoctorNotes()
		);
	}
}
