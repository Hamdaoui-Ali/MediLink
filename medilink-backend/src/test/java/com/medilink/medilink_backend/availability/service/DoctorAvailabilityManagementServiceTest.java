package com.medilink.medilink_backend.availability.service;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.service.DoctorRefNotFoundException;
import com.medilink.medilink_backend.availability.domain.DoctorAvailability;
import com.medilink.medilink_backend.availability.repository.DoctorAvailabilityRepository;
import com.medilink.medilink_backend.availability.web.AvailabilityRequest;
import com.medilink.medilink_backend.availability.web.AvailabilityResponse;
import com.medilink.medilink_backend.blockedslot.repository.BlockedSlotRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorAvailabilityManagementServiceTest {

	private final DoctorAvailabilityRepository availabilityRepository = mock(DoctorAvailabilityRepository.class);
	private final BlockedSlotRepository blockedSlotRepository = mock(BlockedSlotRepository.class);
	private final AppointmentRepository appointmentRepository = mock(AppointmentRepository.class);
	private final DoctorRefRepository doctorRefRepository = mock(DoctorRefRepository.class);
	private final DoctorAvailabilityService service = new DoctorAvailabilityService(
			availabilityRepository,
			blockedSlotRepository,
			appointmentRepository,
			doctorRefRepository
	);

	@Test
	void resolveDoctorReturnsActiveDoctor() {
		DoctorRef doctor = mock(DoctorRef.class);
		when(doctor.isActive()).thenReturn(true);
		when(doctor.getId()).thenReturn(10L);
		when(doctorRefRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorRef resolved = service.resolveDoctor(5L);

		assertEquals(10L, resolved.getId());
	}

	@Test
	void resolveDoctorRejectsMissingDoctor() {
		when(doctorRefRepository.findByUserId(5L)).thenReturn(Optional.empty());

		assertThrows(DoctorRefNotFoundException.class, () -> service.resolveDoctor(5L));
	}

	@Test
	void listAvailabilityReturnsActiveRangesForDoctor() {
		DoctorAvailability monday = availability(1L, 10L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0));
		when(availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(10L))
				.thenReturn(List.of(monday));

		List<AvailabilityResponse> response = service.listAvailability(10L);

		assertEquals(1, response.size());
		assertEquals(1, response.getFirst().dayOfWeek());
		assertEquals(LocalTime.of(9, 0), response.getFirst().startTime());
	}

	@Test
	void createAvailabilitySavesValidRange() {
		AvailabilityRequest request = new AvailabilityRequest(2, LocalTime.of(10, 0), LocalTime.of(14, 0));
		when(availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(10L, 2)).thenReturn(List.of());
		when(availabilityRepository.save(any(DoctorAvailability.class))).thenAnswer(invocation -> {
			DoctorAvailability saved = invocation.getArgument(0);
			setId(saved, 99L);
			return saved;
		});

		AvailabilityResponse response = service.createAvailability(10L, request);

		assertEquals(99L, response.id());
		assertEquals(10L, response.doctorId());
		assertEquals(2, response.dayOfWeek());
	}

	@Test
	void createAvailabilityRejectsOverlappingRange() {
		DoctorAvailability existing = availability(1L, 10L, 3, LocalTime.of(9, 0), LocalTime.of(12, 0));
		when(availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(10L, 3))
				.thenReturn(List.of(existing));

		AvailabilityRequest request = new AvailabilityRequest(3, LocalTime.of(11, 0), LocalTime.of(15, 0));

		assertThrows(InvalidAvailabilityException.class, () -> service.createAvailability(10L, request));
	}

	@Test
	void updateAvailabilityAllowsSameRecordAndChangesFields() {
		DoctorAvailability existing = availability(1L, 10L, 4, LocalTime.of(9, 0), LocalTime.of(12, 0));
		when(availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(10L, 4))
				.thenReturn(List.of(existing));
		when(availabilityRepository.findByIdAndDoctorId(1L, 10L)).thenReturn(Optional.of(existing));

		AvailabilityResponse response = service.updateAvailability(
				10L,
				1L,
				new AvailabilityRequest(4, LocalTime.of(13, 0), LocalTime.of(17, 0))
		);

		assertEquals(LocalTime.of(13, 0), response.startTime());
		assertEquals(LocalTime.of(17, 0), response.endTime());
	}

	@Test
	void updateAvailabilityThrowsWhenMissing() {
		when(availabilityRepository.findByDoctorIdAndDayOfWeekAndActiveTrue(10L, 5)).thenReturn(List.of());
		when(availabilityRepository.findByIdAndDoctorId(99L, 10L)).thenReturn(Optional.empty());

		assertThrows(AvailabilityNotFoundException.class, () -> service.updateAvailability(
				10L,
				99L,
				new AvailabilityRequest(5, LocalTime.of(8, 0), LocalTime.of(10, 0))
		));
	}

	@Test
	void deleteAvailabilityDeactivatesRange() {
		DoctorAvailability existing = availability(1L, 10L, 1, LocalTime.of(9, 0), LocalTime.of(12, 0));
		when(availabilityRepository.findByIdAndDoctorId(1L, 10L)).thenReturn(Optional.of(existing));

		service.deleteAvailability(10L, 1L);

		assertTrue(!existing.isActive());
	}

	@Test
	void createAvailabilityRejectsInvalidTimeRange() {
		AvailabilityRequest request = new AvailabilityRequest(1, LocalTime.of(12, 0), LocalTime.of(10, 0));

		assertThrows(InvalidAvailabilityException.class, () -> service.createAvailability(10L, request));
	}

	private DoctorAvailability availability(Long id, Long doctorId, Integer dayOfWeek, LocalTime start, LocalTime end) {
		DoctorAvailability availability = new DoctorAvailability(doctorId, dayOfWeek, start, end);
		setId(availability, id);
		return availability;
	}

	private void setId(DoctorAvailability availability, Long id) {
		try {
			Field idField = DoctorAvailability.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(availability, id);
		} catch (ReflectiveOperationException exception) {
			throw new RuntimeException(exception);
		}
	}
}
