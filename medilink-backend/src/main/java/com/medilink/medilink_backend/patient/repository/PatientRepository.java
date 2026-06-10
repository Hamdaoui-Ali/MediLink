package com.medilink.medilink_backend.patient.repository;

import com.medilink.medilink_backend.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}
