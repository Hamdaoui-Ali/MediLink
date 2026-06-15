package com.medilink.medilink_backend.doctor.repository;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	Optional<Doctor> findByUserId(Long userId);

	@Query("SELECT d FROM Doctor d JOIN FETCH d.specialty JOIN FETCH d.user WHERE d.status = 'ACTIVE'")
	List<Doctor> findAllActiveWithDetails();

	@Query("SELECT d FROM Doctor d JOIN FETCH d.specialty JOIN FETCH d.user WHERE d.id = :id AND d.status = 'ACTIVE'")
	Optional<Doctor> findByIdAndStatusActive(Long id);

	@Query("SELECT d FROM Doctor d JOIN FETCH d.specialty JOIN FETCH d.user WHERE d.status = 'ACTIVE' AND d.specialty.id = :specialtyId")
	List<Doctor> findBySpecialtyIdAndStatusActive(Long specialtyId);

	@Query("SELECT d FROM Doctor d JOIN FETCH d.specialty JOIN FETCH d.user WHERE d.status = 'ACTIVE' AND LOWER(d.user.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
	List<Doctor> findByUserFullNameContainingIgnoreCaseAndStatusActive(String name);
}
