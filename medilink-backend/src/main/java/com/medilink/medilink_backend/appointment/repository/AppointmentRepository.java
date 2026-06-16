package com.medilink.medilink_backend.appointment.repository;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

	List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc(Long doctorId, AppointmentStatus status);

	List<Appointment> findByDoctorIdAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
			Long doctorId, LocalDate from, LocalDate to);

	List<Appointment> findByDoctorIdAndStatusAndAppointmentDateBetweenOrderByAppointmentDateDescStartTimeDesc(
			Long doctorId, AppointmentStatus status, LocalDate from, LocalDate to);

	Optional<Appointment> findByIdAndDoctorId(Long id, Long doctorId);

	List<Appointment> findByPatientIdOrderByAppointmentDateDescStartTimeDesc(Long patientId);

	List<Appointment> findByPatientIdAndAppointmentDateGreaterThanEqualOrderByAppointmentDateAscStartTimeAsc(
			Long patientId, LocalDate date);

	List<Appointment> findByPatientIdAndAppointmentDateLessThanOrderByAppointmentDateDescStartTimeDesc(
			Long patientId, LocalDate date);

	List<Appointment> findByStatusAndAppointmentDateBetweenOrderByAppointmentDateAsc(
			AppointmentStatus status, LocalDate from, LocalDate to);

	List<Appointment> findByDoctorIdAndAppointmentDateAndStatusIn(
			Long doctorId, LocalDate date, List<AppointmentStatus> statuses);
}
