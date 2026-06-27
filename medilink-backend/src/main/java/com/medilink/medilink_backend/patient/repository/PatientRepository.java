package com.medilink.medilink_backend.patient.repository;

import com.medilink.medilink_backend.appointment.domain.PatientRef;
import com.medilink.medilink_backend.patient.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

	@Query("""
		SELECT new com.medilink.medilink_backend.appointment.domain.PatientRef(
			p.id,
			u.fullName,
			u.email,
			u.phoneNumber,
			p.dateOfBirth,
			p.gender,
			p.address
		)
		FROM Patient p
		JOIN p.user u
		WHERE p.id IN :ids
	""")
	List<PatientRef> findPatientRefsByIds(@Param("ids") List<Long> ids);

	@Query("""
		SELECT p
		FROM Patient p
		JOIN FETCH p.user u
		ORDER BY u.fullName ASC
	""")
	List<Patient> findAllWithUser();

	@Query("""
		SELECT p
		FROM Patient p
		JOIN FETCH p.user u
		WHERE p.id = :id
	""")
	Optional<Patient> findByIdWithUser(@Param("id") Long id);
}
