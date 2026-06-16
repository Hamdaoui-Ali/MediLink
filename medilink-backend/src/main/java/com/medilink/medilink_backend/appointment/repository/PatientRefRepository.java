package com.medilink.medilink_backend.appointment.repository;

import com.medilink.medilink_backend.appointment.domain.PatientRefEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRefRepository extends JpaRepository<PatientRefEntity, Long> {

	Optional<PatientRefEntity> findByUserId(Long userId);
}
