package com.medilink.medilink_backend.doctor.repository;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	@Query("""
			select doctor
			from Doctor doctor
			join fetch doctor.user
			join fetch doctor.specialty
			order by doctor.user.fullName asc
			""")
	List<Doctor> findAllWithUserAndSpecialtyOrderByUserFullNameAsc();

	@Query("""
			select doctor
			from Doctor doctor
			join fetch doctor.user
			join fetch doctor.specialty
			where doctor.id = :id
			""")
	Optional<Doctor> findByIdWithUserAndSpecialty(Long id);
}
