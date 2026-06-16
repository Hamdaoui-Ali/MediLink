package com.medilink.medilink_backend.patient.repository;

import com.medilink.medilink_backend.appointment.domain.PatientRef;
import com.medilink.medilink_backend.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

	@Query("""
		SELECT new com.medilink.medilink_backend.appointment.domain.PatientRef(p.id, u.fullName)
		FROM Patient p
		JOIN p.user u
		WHERE p.id IN :ids
	""")
	List<PatientRef> findPatientNamesByIds(@Param("ids") List<Long> ids);
}
