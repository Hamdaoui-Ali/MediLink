package com.medilink.medilink_backend.administration.repository;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

	boolean existsByNameIgnoreCase(String name);

	boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

	List<Specialty> findAllByOrderByNameAsc();

	List<Specialty> findAllByStatusOrderByNameAsc(SpecialtyStatus status);

	Optional<Specialty> findByIdAndStatus(Long id, SpecialtyStatus status);
}
