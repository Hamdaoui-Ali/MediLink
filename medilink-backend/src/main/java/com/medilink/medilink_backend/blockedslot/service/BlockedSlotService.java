package com.medilink.medilink_backend.blockedslot.service;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.service.DoctorRefNotFoundException;
import com.medilink.medilink_backend.blockedslot.domain.BlockedSlot;
import com.medilink.medilink_backend.blockedslot.repository.BlockedSlotRepository;
import com.medilink.medilink_backend.blockedslot.web.BlockedSlotRequest;
import com.medilink.medilink_backend.blockedslot.web.BlockedSlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BlockedSlotService {

	private final BlockedSlotRepository blockedSlotRepository;
	private final DoctorRefRepository doctorRefRepository;

	public BlockedSlotService(
			BlockedSlotRepository blockedSlotRepository,
			DoctorRefRepository doctorRefRepository
	) {
		this.blockedSlotRepository = blockedSlotRepository;
		this.doctorRefRepository = doctorRefRepository;
	}

	public DoctorRef resolveDoctor(Long userId) {
		DoctorRef doctor = doctorRefRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorRefNotFoundException(userId));

		if (!doctor.isActive()) {
			throw new DoctorRefNotFoundException(userId);
		}

		return doctor;
	}

	@Transactional(readOnly = true)
	public List<BlockedSlotResponse> listBlockedSlots(Long doctorId) {
		return blockedSlotRepository.findByDoctorIdAndActiveTrueOrderByBlockDateDescStartTimeDesc(doctorId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public BlockedSlotResponse createBlockedSlot(Long doctorId, BlockedSlotRequest request) {
		if (!request.endTime().isAfter(request.startTime())) {
			throw new InvalidBlockedSlotException("End time must be after start time.");
		}

		if (request.blockDate().isBefore(java.time.LocalDate.now())) {
			throw new InvalidBlockedSlotException("Cannot block slots in the past.");
		}

		BlockedSlot slot = new BlockedSlot(
				doctorId,
				request.blockDate(),
				request.startTime(),
				request.endTime(),
				request.reason()
		);
		BlockedSlot saved = blockedSlotRepository.save(slot);
		return toResponse(saved);
	}

	@Transactional
	public BlockedSlotResponse updateBlockedSlot(Long doctorId, Long slotId, BlockedSlotRequest request) {
		if (!request.endTime().isAfter(request.startTime())) {
			throw new InvalidBlockedSlotException("End time must be after start time.");
		}

		if (request.blockDate().isBefore(java.time.LocalDate.now())) {
			throw new InvalidBlockedSlotException("Cannot block slots in the past.");
		}

		BlockedSlot slot = blockedSlotRepository.findByIdAndDoctorId(slotId, doctorId)
				.orElseThrow(() -> new BlockedSlotNotFoundException(slotId));

		slot.update(request.blockDate(), request.startTime(), request.endTime(), request.reason());
		return toResponse(slot);
	}

	@Transactional
	public void deleteBlockedSlot(Long doctorId, Long slotId) {
		BlockedSlot slot = blockedSlotRepository.findByIdAndDoctorId(slotId, doctorId)
				.orElseThrow(() -> new BlockedSlotNotFoundException(slotId));

		slot.deactivate();
	}

	private BlockedSlotResponse toResponse(BlockedSlot slot) {
		return new BlockedSlotResponse(
				slot.getId(),
				slot.getDoctorId(),
				slot.getBlockDate(),
				slot.getStartTime(),
				slot.getEndTime(),
				slot.getReason()
		);
	}
}
