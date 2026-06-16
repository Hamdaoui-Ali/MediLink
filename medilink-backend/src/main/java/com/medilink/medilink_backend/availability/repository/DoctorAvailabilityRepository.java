package com.medilink.medilink_backend.availability.repository;

import com.medilink.medilink_backend.availability.domain.DoctorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DoctorAvailabilityRepository extends JpaRepository<DoctorAvailability, Long> {

	List<DoctorAvailability> findByDoctorIdAndDayOfWeekAndActiveTrue(Long doctorId, Integer dayOfWeek);
}
