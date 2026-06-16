package com.medilink.medilink_backend.administration.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import com.medilink.medilink_backend.administration.repository.SpecialtyRepository;
import com.medilink.medilink_backend.administration.web.SpecialtyRequest;
import com.medilink.medilink_backend.administration.web.SpecialtyResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SpecialtyServiceTest {

	private final SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);
	private final SpecialtyService specialtyService = new SpecialtyService(specialtyRepository);

	@Test
	void listReturnsOnlyActiveSpecialtiesWhenRequested() {
		when(specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE))
				.thenReturn(List.of(new Specialty("Cardiology", "Heart care")));

		List<SpecialtyResponse> specialties = specialtyService.list(true);

		assertEquals(1, specialties.size());
		assertEquals("Cardiology", specialties.getFirst().name());
		assertEquals(SpecialtyStatus.ACTIVE, specialties.getFirst().status());
	}

	@Test
	void createNormalizesNameAndDescription() {
		when(specialtyRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(false);
		when(specialtyRepository.save(any(Specialty.class))).thenAnswer(invocation -> invocation.getArgument(0));

		SpecialtyResponse specialty = specialtyService.create(new SpecialtyRequest("  Cardiology  ", "  Heart care  "));

		assertEquals("Cardiology", specialty.name());
		assertEquals("Heart care", specialty.description());
		assertEquals(SpecialtyStatus.ACTIVE, specialty.status());
	}

	@Test
	void createRejectsDuplicateName() {
		when(specialtyRepository.existsByNameIgnoreCase("Cardiology")).thenReturn(true);

		assertThrows(
				DuplicateSpecialtyNameException.class,
				() -> specialtyService.create(new SpecialtyRequest("Cardiology", null))
		);

		verify(specialtyRepository, never()).save(any(Specialty.class));
	}

	@Test
	void updateRejectsNameUsedByAnotherSpecialty() {
		Specialty specialty = new Specialty("General Medicine", null);
		when(specialtyRepository.findById(10L)).thenReturn(Optional.of(specialty));
		when(specialtyRepository.existsByNameIgnoreCaseAndIdNot("Cardiology", 10L)).thenReturn(true);

		assertThrows(
				DuplicateSpecialtyNameException.class,
				() -> specialtyService.update(10L, new SpecialtyRequest("Cardiology", null))
		);
	}

	@Test
	void activateAndDeactivateChangeStatusWithoutDeletingSpecialty() {
		Specialty specialty = new Specialty("Cardiology", null);
		when(specialtyRepository.findById(10L)).thenReturn(Optional.of(specialty));

		SpecialtyResponse inactive = specialtyService.deactivate(10L);
		SpecialtyResponse active = specialtyService.activate(10L);

		assertEquals(SpecialtyStatus.INACTIVE, inactive.status());
		assertEquals(SpecialtyStatus.ACTIVE, active.status());
		verify(specialtyRepository, never()).delete(any());
	}

	@Test
	void getThrowsWhenSpecialtyDoesNotExist() {
		when(specialtyRepository.findById(10L)).thenReturn(Optional.empty());

		assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.get(10L));
	}

	@Test
	void getActiveSpecialtyReturnsOnlyActiveSpecialtiesForDoctorProfiles() {
		Specialty specialty = new Specialty("Cardiology", null);
		when(specialtyRepository.findByIdAndStatus(10L, SpecialtyStatus.ACTIVE)).thenReturn(Optional.of(specialty));

		Specialty activeSpecialty = specialtyService.getActiveSpecialty(10L);

		assertSame(specialty, activeSpecialty);
	}

	@Test
	void getActiveSpecialtyRejectsInactiveOrMissingSpecialties() {
		when(specialtyRepository.findByIdAndStatus(10L, SpecialtyStatus.ACTIVE)).thenReturn(Optional.empty());

		assertThrows(SpecialtyNotFoundException.class, () -> specialtyService.getActiveSpecialty(10L));
	}

	@Test
	void trimToNullTrimsValueAndConvertsBlankToNull() {
		assertEquals("Heart care", specialtyService.trimToNull("  Heart care "));
		assertNull(specialtyService.trimToNull(""));
		assertNull(specialtyService.trimToNull(null));
	}
}
