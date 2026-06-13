package com.medilink.medilink_backend.appointment.repository;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByDoctorIdOrderByAppointmentDateDescStartTimeDesc(Long doctorId);

	List<Appointment> findByDoctorIdAndStatusOrderByAppointmentDateDescStartTimeDesc(Long doctorId, AppointmentStatus status);

	Optional<Appointment> findByIdAndDoctorId(Long id, Long doctorId);
}
