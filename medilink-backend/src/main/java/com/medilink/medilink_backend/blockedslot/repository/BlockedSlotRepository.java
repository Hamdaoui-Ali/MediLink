package com.medilink.medilink_backend.blockedslot.repository;

import com.medilink.medilink_backend.blockedslot.domain.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {

	List<BlockedSlot> findByDoctorIdAndActiveTrueOrderByBlockDateDescStartTimeDesc(Long doctorId);

	Optional<BlockedSlot> findByIdAndDoctorId(Long id, Long doctorId);

	List<BlockedSlot> findByDoctorIdAndBlockDateAndActiveTrue(Long doctorId, LocalDate blockDate);
}
