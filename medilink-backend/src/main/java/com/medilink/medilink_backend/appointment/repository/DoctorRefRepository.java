package com.medilink.medilink_backend.appointment.repository;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DoctorRefRepository extends JpaRepository<DoctorRef, Long> {

	Optional<DoctorRef> findByUserId(Long userId);

	@Query("SELECT d.consultationDurationMinutes FROM DoctorRef d WHERE d.id = :doctorId")
	Optional<Integer> findConsultationDurationById(Long doctorId);
}
