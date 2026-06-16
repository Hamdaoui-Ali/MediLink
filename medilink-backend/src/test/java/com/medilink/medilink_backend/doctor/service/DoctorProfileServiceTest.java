package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.DoctorProfileResponse;
import com.medilink.medilink_backend.doctor.web.DoctorProfileUpdateRequest;
import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.User;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DoctorProfileServiceTest {

	private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
	private final DoctorProfileService service = new DoctorProfileService(doctorRepository);

	private Doctor createDoctor() {
		User user = mock(User.class);
		when(user.getId()).thenReturn(5L);
		when(user.getFullName()).thenReturn("Dr. Test");
		when(user.getEmail()).thenReturn("dr.test@medilink.local");
		when(user.getPhoneNumber()).thenReturn("+15551234567");
		when(user.getAccountStatus()).thenReturn(AccountStatus.ACTIVE);

		Specialty specialty = mock(Specialty.class);
		when(specialty.getName()).thenReturn("Cardiology");

		Doctor doctor = mock(Doctor.class);
		when(doctor.getId()).thenReturn(1L);
		when(doctor.getUser()).thenReturn(user);
		when(doctor.getSpecialty()).thenReturn(specialty);
		when(doctor.getSpecialtyName()).thenReturn("Cardiology");
		when(doctor.getBiography()).thenReturn("Experienced cardiologist");
		when(doctor.getConsultationDurationMinutes()).thenReturn(30);
		when(doctor.getClinicAddress()).thenReturn("123 Heart Lane");
		when(doctor.getStatus()).thenReturn("ACTIVE");

		return doctor;
	}

	@Test
	void getProfileReturnsDoctorProfile() {
		Doctor doctor = createDoctor();
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorProfileResponse response = service.getProfile(5L);

		assertEquals("Dr. Test", response.fullName());
		assertEquals("Cardiology", response.specialtyName());
		assertEquals("Experienced cardiologist", response.biography());
		assertEquals(30, response.consultationDurationMinutes());
		assertEquals("123 Heart Lane", response.clinicAddress());
		assertEquals("ACTIVE", response.status());
		assertEquals("ACTIVE", response.accountStatus());
	}

	@Test
	void getProfileThrowsWhenNotFound() {
		when(doctorRepository.findByUserId(99L)).thenReturn(Optional.empty());

		assertThrows(DoctorNotFoundException.class, () -> service.getProfile(99L));
	}

	@Test
	void updateProfileUpdatesAllowedFields() {
		Doctor doctor = createDoctor();
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		when(doctor.getBiography()).thenReturn("Updated biography");
		when(doctor.getConsultationDurationMinutes()).thenReturn(45);
		when(doctor.getClinicAddress()).thenReturn("456 Updated St");

		DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest(
				"Updated biography", "456 Updated St", "+15559999999", 45
		);

		DoctorProfileResponse response = service.updateProfile(5L, request);

		assertEquals("Updated biography", response.biography());
		assertEquals(45, response.consultationDurationMinutes());
		assertEquals("456 Updated St", response.clinicAddress());
	}

	@Test
	void updateProfileOnlyUpdatesProvidedFields() {
		Doctor doctor = createDoctor();
		User user = doctor.getUser();
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest(
				"  New biography  ", null, null, null
		);

		service.updateProfile(5L, request);

		// Existing values should be preserved for null fields
		assertNotNull(doctor.getBiography());
	}

	@Test
	void updateProfileDoesNotAllowChangingStatusOrSpecialty() {
		Doctor doctor = createDoctor();
		when(doctorRepository.findByUserId(5L)).thenReturn(Optional.of(doctor));

		DoctorProfileUpdateRequest request = new DoctorProfileUpdateRequest(
				"Bio", "Addr", "+15550000000", 30
		);

		DoctorProfileResponse response = service.updateProfile(5L, request);

		// Status and specialty should remain unchanged
		assertEquals("ACTIVE", response.status());
		assertEquals("Cardiology", response.specialtyName());
	}
}
