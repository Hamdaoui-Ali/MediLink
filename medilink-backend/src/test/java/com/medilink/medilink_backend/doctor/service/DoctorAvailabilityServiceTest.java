package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.domain.DoctorAvailability;
import com.medilink.medilink_backend.doctor.repository.DoctorAvailabilityRepository;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.DoctorAvailabilityRequest;
import com.medilink.medilink_backend.doctor.web.DoctorAvailabilityResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorAvailabilityServiceTest {

	private final DoctorAvailabilityRepository availabilityRepository = mock(DoctorAvailabilityRepository.class);
	private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
	private final DoctorAvailabilityService service = new DoctorAvailabilityService(availabilityRepository, doctorRepository);

	@Test
	void resolveDoctorReturnsActiveDoctorByUserId() {
		Doctor doctor = mock(Doctor.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctor.getId()).thenReturn(1L);
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		Doctor resolved = service.resolveDoctor(5L);

		assertEquals(1L, resolved.getId());
	}

	@Test
	void resolveDoctorThrowsWhenDoctorNotFound() {
		when(doctorRepository.findByUserId(99L)).thenReturn(Optional.empty());

		assertThrows(DoctorNotFoundException.class, () -> service.resolveDoctor(99L));
	}

	@Test
	void resolveDoctorThrowsWhenDoctorIsInactive() {
		Doctor doctor = mock(Doctor.class);
		when(doctor.isActive()).thenReturn(false);
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		assertThrows(DoctorNotFoundException.class, () -> service.resolveDoctor(5L));
	}

	@Test
	void listAvailabilityReturnsActiveSlotsForDoctor() {
		DoctorAvailability slot = new DoctorAvailability(1L, 1, LocalTime.of(9, 0), LocalTime.of(17, 0));
		when(availabilityRepository.findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(1L))
				.thenReturn(List.of(slot));

		List<DoctorAvailabilityResponse> slots = service.listAvailability(1L);

		assertEquals(1, slots.size());
		assertEquals(1, slots.getFirst().dayOfWeek());
		assertEquals(LocalTime.of(9, 0), slots.getFirst().startTime());
		assertTrue(slots.getFirst().isActive());
	}

	@Test
	void addAvailabilityCreatesNewSlot() {
		when(availabilityRepository.save(any(DoctorAvailability.class)))
				.thenAnswer(inv -> {
					DoctorAvailability a = inv.getArgument(0);
					return new DoctorAvailability(a.getDoctorId(), a.getDayOfWeek(), a.getStartTime(), a.getEndTime()) {
						@Override
						public Long getId() { return 10L; }
					};
				});

		DoctorAvailabilityResponse response = service.addAvailability(1L,
				new DoctorAvailabilityRequest(3, "09:00", "17:00"));

		assertEquals(10L, response.id());
		assertEquals(3, response.dayOfWeek());
		assertEquals(LocalTime.of(9, 0), response.startTime());
		assertEquals(LocalTime.of(17, 0), response.endTime());
		assertTrue(response.isActive());
	}

	@Test
	void addAvailabilityRejectsInvalidTimeRange() {
		assertThrows(InvalidAvailabilityException.class, () ->
				service.addAvailability(1L, new DoctorAvailabilityRequest(1, "17:00", "09:00")));
	}

	@Test
	void addAvailabilityRejectsSameStartAndEndTime() {
		assertThrows(InvalidAvailabilityException.class, () ->
				service.addAvailability(1L, new DoctorAvailabilityRequest(1, "12:00", "12:00")));
	}

	@Test
	void addAvailabilityRejectsInvalidTimeFormat() {
		assertThrows(InvalidAvailabilityException.class, () ->
				service.addAvailability(1L, new DoctorAvailabilityRequest(1, "9am", "5pm")));
	}

	@Test
	void updateAvailabilityModifiesExistingSlot() {
		DoctorAvailability existing = new DoctorAvailability(1L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0));
		when(availabilityRepository.findByIdAndDoctorId(10L, 1L)).thenReturn(Optional.of(existing));

		DoctorAvailabilityResponse response = service.updateAvailability(1L, 10L,
				new DoctorAvailabilityRequest(5, "14:00", "18:00"));

		assertEquals(5, response.dayOfWeek());
		assertEquals(LocalTime.of(14, 0), response.startTime());
		assertEquals(LocalTime.of(18, 0), response.endTime());
	}

	@Test
	void updateAvailabilityThrowsWhenSlotNotFound() {
		when(availabilityRepository.findByIdAndDoctorId(99L, 1L)).thenReturn(Optional.empty());

		assertThrows(AvailabilityNotFoundException.class, () ->
				service.updateAvailability(1L, 99L, new DoctorAvailabilityRequest(1, "09:00", "17:00")));
	}

	@Test
	void deactivateAvailabilitySetsInactive() {
		DoctorAvailability existing = new DoctorAvailability(1L, 2, LocalTime.of(9, 0), LocalTime.of(13, 0));
		when(availabilityRepository.findByIdAndDoctorId(5L, 1L)).thenReturn(Optional.of(existing));

		DoctorAvailabilityResponse response = service.deactivateAvailability(1L, 5L);

		assertFalse(response.isActive());
	}

	@Test
	void deactivateAvailabilityThrowsWhenSlotNotFound() {
		when(availabilityRepository.findByIdAndDoctorId(99L, 1L)).thenReturn(Optional.empty());

		assertThrows(AvailabilityNotFoundException.class, () ->
				service.deactivateAvailability(1L, 99L));
	}

	@Test
	void listAvailabilityReturnsEmptyListWhenNoSlotsExist() {
		when(availabilityRepository.findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(1L))
				.thenReturn(List.of());

		List<DoctorAvailabilityResponse> slots = service.listAvailability(1L);

		assertTrue(slots.isEmpty());
	}
}
