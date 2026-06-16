package com.medilink.medilink_backend.appointment.service;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.domain.PatientRef;
import com.medilink.medilink_backend.appointment.domain.PatientRefEntity;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.repository.PatientRefRepository;
import com.medilink.medilink_backend.appointment.web.AppointmentResponse;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

	private final AppointmentRepository appointmentRepository;
	private final DoctorRefRepository doctorRefRepository;
	private final PatientRefRepository patientRefRepository;
	private final PatientRepository patientRepository;

	public AppointmentService(
			AppointmentRepository appointmentRepository,
			DoctorRefRepository doctorRefRepository,
			PatientRefRepository patientRefRepository,
			PatientRepository patientRepository
	) {
		this.appointmentRepository = appointmentRepository;
		this.doctorRefRepository = doctorRefRepository;
		this.patientRefRepository = patientRefRepository;
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

	public PatientRefEntity resolvePatient(Long userId) {
		return patientRefRepository.findByUserId(userId)
				.orElseThrow(() -> new PatientNotFoundException(userId));
	}

	@Transactional
	public AppointmentResponse createAppointment(Long patientId, Long doctorId,
			LocalDate appointmentDate, LocalTime startTime, String reason) {
		if (appointmentDate.isBefore(LocalDate.now())) {
			throw new SlotUnavailableException("Cannot book an appointment in the past.");
		}

		if (appointmentDate.equals(LocalDate.now()) && startTime.isBefore(LocalTime.now())) {
			throw new SlotUnavailableException("Cannot book a time slot that has already passed today.");
		}

		DoctorRef doctor = doctorRefRepository.findById(doctorId)
				.orElseThrow(() -> new DoctorRefNotFoundException(doctorId));

		if (!doctor.isActive()) {
			throw new SlotUnavailableException("The selected doctor is not currently available.");
		}

		int durationMinutes = doctorRefRepository.findConsultationDurationById(doctorId).orElse(30);

		LocalTime endTime = startTime.plusMinutes(durationMinutes);

		List<AppointmentStatus> activeStatuses = List.of(
				AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED);
		List<Appointment> conflicts = appointmentRepository
				.findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, appointmentDate, activeStatuses);

		for (Appointment existing : conflicts) {
			if (startTime.isBefore(existing.getEndTime()) && endTime.isAfter(existing.getStartTime())) {
				throw new SlotUnavailableException(
						"This time slot is no longer available. Please choose another slot.");
			}
		}

		Appointment appointment = new Appointment(
				doctorId, patientId, appointmentDate, startTime, endTime, reason);
		appointment = appointmentRepository.save(appointment);

		return toResponse(appointment);
	}

	@Transactional(readOnly = true)
	public List<AppointmentResponse> listPatientAppointments(Long patientId, String filter) {
		List<Appointment> appointments;
		LocalDate today = LocalDate.now();

		if ("upcoming".equalsIgnoreCase(filter)) {
			appointments = appointmentRepository
					.findByPatientIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscStartTimeAsc(
							patientId, today);
		} else if ("past".equalsIgnoreCase(filter)) {
			appointments = appointmentRepository
					.findByPatientIdAndAppointmentDateLessThanOrderByAppointmentDateDescStartTimeDesc(
							patientId, today);
		} else {
			appointments = appointmentRepository
					.findByPatientIdOrderByAppointmentDateDescStartTimeDesc(patientId);
		}

		return toResponses(appointments);
	}

	@Transactional(readOnly = true)
	public AppointmentResponse getPatientAppointment(Long patientId, Long appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.filter(a -> a.getPatientId().equals(patientId))
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));
		return toResponse(appointment);
	}

	@Transactional
	public AppointmentResponse cancelAppointment(Long patientId, Long appointmentId) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.filter(a -> a.getPatientId().equals(patientId))
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

		if (!appointment.getStatus().canTransitionTo(AppointmentStatus.CANCELLED)) {
			throw new InvalidAppointmentStatusException(
					"Cannot cancel appointment " + appointmentId + " with status " + appointment.getStatus());
		}

		appointment.updateStatus(AppointmentStatus.CANCELLED);
		return toResponse(appointment);
	}

	@Transactional
	public AppointmentResponse rescheduleAppointment(Long patientId, Long appointmentId,
			LocalDate newDate, LocalTime newStartTime) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.filter(a -> a.getPatientId().equals(patientId))
				.orElseThrow(() -> new AppointmentNotFoundException(appointmentId));

		if (!appointment.getStatus().canTransitionTo(AppointmentStatus.RESCHEDULED)) {
			throw new InvalidAppointmentStatusException(
					"Cannot reschedule appointment " + appointmentId + " with status " + appointment.getStatus());
		}

		if (newDate.isBefore(LocalDate.now())) {
			throw new SlotUnavailableException("Cannot reschedule to a past date.");
		}

		Long doctorId = appointment.getDoctorId();
		int durationMinutes = doctorRefRepository.findConsultationDurationById(doctorId).orElse(30);
		LocalTime newEndTime = newStartTime.plusMinutes(durationMinutes);

		List<AppointmentStatus> activeStatuses = List.of(
				AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED);
		List<Appointment> conflicts = appointmentRepository
				.findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, newDate, activeStatuses);

		for (Appointment existing : conflicts) {
			if (!existing.getId().equals(appointmentId)
					&& newStartTime.isBefore(existing.getEndTime())
					&& newEndTime.isAfter(existing.getStartTime())) {
				throw new SlotUnavailableException(
						"The requested time slot is not available.");
			}
		}

		appointment.updateStatus(AppointmentStatus.RESCHEDULED);

		Appointment newAppointment = new Appointment(
				doctorId, patientId, newDate, newStartTime, newEndTime, appointment.getReason());
		newAppointment = appointmentRepository.save(newAppointment);

		return toResponse(newAppointment);
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
