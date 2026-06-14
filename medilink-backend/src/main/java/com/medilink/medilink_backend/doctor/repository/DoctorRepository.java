package com.medilink.medilink_backend.doctor.repository;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

	Optional<Doctor> findByUserId(Long userId);
}
