package com.medilink.medilink_backend.blockedslot.service;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.service.DoctorRefNotFoundException;
import com.medilink.medilink_backend.blockedslot.domain.BlockedSlot;
import com.medilink.medilink_backend.blockedslot.repository.BlockedSlotRepository;
import com.medilink.medilink_backend.blockedslot.web.BlockedSlotRequest;
import com.medilink.medilink_backend.blockedslot.web.BlockedSlotResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BlockedSlotServiceTest {

	private final BlockedSlotRepository blockedSlotRepository = mock(BlockedSlotRepository.class);
	private final DoctorRefRepository doctorRefRepository = mock(DoctorRefRepository.class);
	private final BlockedSlotService service = new BlockedSlotService(blockedSlotRepository, doctorRefRepository);

	@Test
	void resolveDoctorReturnsActiveDoctor() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctor.getId()).thenReturn(1L);
		when(doctorRefRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorRef resolved = service.resolveDoctor(5L);

		assertEquals(1L, resolved.getId());
	}

	@Test
	void resolveDoctorThrowsWhenNotFound() {
		when(doctorRefRepository.findByUserId(99L)).thenReturn(Optional.empty());

		assertThrows(DoctorRefNotFoundException.class, () -> service.resolveDoctor(99L));
	}

	@Test
	void listBlockedSlotsReturnsActiveSlotsForDoctor() {
		BlockedSlot slot = new BlockedSlot(1L, LocalDate.of(2026, 6, 20),
				LocalTime.of(14, 0), LocalTime.of(16, 0), "Vacation");
		when(blockedSlotRepository.findByDoctorIdAndActiveTrueOrderByBlockDateDescStartTimeDesc(1L))
				.thenReturn(List.of(slot));

		List<BlockedSlotResponse> slots = service.listBlockedSlots(1L);

		assertEquals(1, slots.size());
		assertEquals("Vacation", slots.getFirst().reason());
	}

	@Test
	void createBlockedSlotSavesValidSlot() {
		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), "Meeting"
		);
		when(blockedSlotRepository.save(any(BlockedSlot.class))).thenAnswer(invocation -> {
			BlockedSlot saved = invocation.getArgument(0);
			try {
				java.lang.reflect.Field idField = BlockedSlot.class.getDeclaredField("id");
				idField.setAccessible(true);
				idField.set(saved, 100L);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return saved;
		});

		BlockedSlotResponse response = service.createBlockedSlot(1L, request);

		assertNotNull(response);
		assertEquals(LocalDate.now().plusDays(1), response.blockDate());
		assertEquals("Meeting", response.reason());
	}

	@Test
	void createBlockedSlotRejectsEndTimeBeforeStartTime() {
		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(10, 0), "Invalid"
		);

		assertThrows(InvalidBlockedSlotException.class, () -> service.createBlockedSlot(1L, request));
	}

	@Test
	void createBlockedSlotRejectsPastDate() {
		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.now().minusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), "Past"
		);

		assertThrows(InvalidBlockedSlotException.class, () -> service.createBlockedSlot(1L, request));
	}

	@Test
	void deleteBlockedSlotDeactivatesSlot() {
		BlockedSlot slot = new BlockedSlot(1L, LocalDate.of(2026, 6, 20),
				LocalTime.of(14, 0), LocalTime.of(16, 0), "Vacation");
		when(blockedSlotRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(slot));

		service.deleteBlockedSlot(1L, 10L);

		assertTrue(!slot.isActive());
	}

	@Test
	void deleteBlockedSlotThrowsWhenNotFound() {
		when(blockedSlotRepository.findByIdAndDoctorId(99L, 1L)).thenReturn(Optional.empty());

		assertThrows(BlockedSlotNotFoundException.class, () -> service.deleteBlockedSlot(1L, 99L));
	}

	@Test
	void updateBlockedSlotUpdatesFields() {
		BlockedSlot slot = new BlockedSlot(1L, LocalDate.of(2026, 6, 20),
				LocalTime.of(10, 0), LocalTime.of(12, 0), "Old reason");
		when(blockedSlotRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(slot));

		BlockedSlotRequest updateRequest = new BlockedSlotRequest(
				LocalDate.now().plusDays(2), LocalTime.of(9, 0), LocalTime.of(11, 0), "Updated reason"
		);

		BlockedSlotResponse response = service.updateBlockedSlot(1L, 10L, updateRequest);

		assertEquals(LocalDate.now().plusDays(2), response.blockDate());
		assertEquals(LocalTime.of(9, 0), response.startTime());
		assertEquals(LocalTime.of(11, 0), response.endTime());
		assertEquals("Updated reason", response.reason());
	}

	@Test
	void updateBlockedSlotRejectsEndTimeBeforeStartTime() {
		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(10, 0), "Invalid"
		);

		assertThrows(InvalidBlockedSlotException.class,
				() -> service.updateBlockedSlot(1L, 10L, request));
	}

	@Test
	void updateBlockedSlotThrowsWhenNotFound() {
		when(blockedSlotRepository.findByIdAndDoctorId(99L, 1L)).thenReturn(Optional.empty());

		BlockedSlotRequest request = new BlockedSlotRequest(
				LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(12, 0), "Test"
		);

		assertThrows(BlockedSlotNotFoundException.class,
				() -> service.updateBlockedSlot(1L, 99L, request));
	}
}
