package com.medilink.medilink_backend.doctor.repository;

import com.medilink.medilink_backend.doctor.domain.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

	List<DoctorAvailability> findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(Long doctorId);

	Optional<DoctorAvailability> findByIdAndDoctorId(Long id, Long doctorId);

	boolean existsByDoctorIdAndDayOfWeekAndIsActiveTrue(Long doctorId, int dayOfWeek);
}
