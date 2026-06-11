package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.domain.DoctorStatus;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.AdminDoctorRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorResponse;
import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.service.CreateUserCommand;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.UserAccountService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorManagementServiceTest {

	private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
	private final SpecialtyService specialtyService = mock(SpecialtyService.class);
	private final UserAccountService userAccountService = mock(UserAccountService.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final DoctorManagementService doctorManagementService = new DoctorManagementService(
			doctorRepository,
			specialtyService,
			userAccountService,
			userRepository,
			passwordEncoder
	);

	@Test
	void createBuildsDoctorUserAndProfileWithNormalizedOptionalFields() {
		Specialty specialty = new Specialty("Cardiology", "Heart care");
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", "+1555");
		when(specialtyService.getActiveSpecialty(1L)).thenReturn(specialty);
		when(passwordEncoder.encode("Doctor@123")).thenReturn("encoded-password");
		when(userAccountService.createUser(any(CreateUserCommand.class))).thenReturn(user);
		when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AdminDoctorResponse response = doctorManagementService.create(new AdminDoctorRequest(
				"  Dr Jane  ",
				"  DOCTOR@Example.COM ",
				" Doctor@123 ",
				" +1555 ",
				1L,
				"  Bio  ",
				30,
				"  Clinic  "
		));

		ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
		verify(userAccountService).createUser(commandCaptor.capture());
		assertEquals(RoleName.DOCTOR, commandCaptor.getValue().roleName());
		assertEquals("encoded-password", commandCaptor.getValue().passwordHash());
		assertEquals("Bio", response.biography());
		assertEquals("Clinic", response.clinicAddress());
		assertEquals(DoctorStatus.ACTIVE, response.status());
	}

	@Test
	void createRequiresPasswordBeforeCreatingUser() {
		assertThrows(InvalidDoctorRequestException.class, () -> doctorManagementService.create(new AdminDoctorRequest(
				"Dr Jane",
				"doctor@example.com",
				" ",
				null,
				1L,
				null,
				30,
				null
		)));

		verify(userAccountService, never()).createUser(any(CreateUserCommand.class));
		verify(doctorRepository, never()).save(any(Doctor.class));
	}

	@Test
	void listReturnsDoctorsSortedByRepositoryQuery() {
		Specialty specialty = new Specialty("Dermatology", null);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr List", "list@example.com", "hash", null);
		Doctor doctor = new Doctor(user, specialty, null, 45, null);
		when(doctorRepository.findAllWithUserAndSpecialtyOrderByUserFullNameAsc()).thenReturn(List.of(doctor));

		List<AdminDoctorResponse> doctors = doctorManagementService.list();

		assertEquals(1, doctors.size());
		assertEquals("Dr List", doctors.getFirst().fullName());
		assertEquals("Dermatology", doctors.getFirst().specialtyName());
	}

	@Test
	void updateChangesUserProfilePasswordAndDoctorProfile() {
		Specialty oldSpecialty = new Specialty("General Medicine", null);
		Specialty newSpecialty = new Specialty("Cardiology", null);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Old", "old@example.com", "old-hash", null);
		Doctor doctor = new Doctor(user, oldSpecialty, "Old bio", 30, null);
		when(doctorRepository.findByIdWithUserAndSpecialty(10L)).thenReturn(Optional.of(doctor));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot("new@example.com", user.getId())).thenReturn(false);
		when(passwordEncoder.encode("NewPass@123")).thenReturn("new-hash");
		when(specialtyService.getActiveSpecialty(2L)).thenReturn(newSpecialty);

		AdminDoctorResponse response = doctorManagementService.update(10L, new AdminDoctorRequest(
				"  Dr New  ",
				"  NEW@Example.COM ",
				" NewPass@123 ",
				" +1666 ",
				2L,
				"  New bio  ",
				60,
				"  New clinic  "
		));

		assertEquals("Dr New", response.fullName());
		assertEquals("new@example.com", response.email());
		assertEquals("+1666", response.phoneNumber());
		assertEquals("new-hash", user.getPasswordHash());
		assertEquals("Cardiology", response.specialtyName());
		assertEquals(60, response.consultationDurationMinutes());
	}

	@Test
	void updateRejectsDuplicateEmailForAnotherUser() {
		Specialty specialty = new Specialty("Cardiology", null);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", null);
		Doctor doctor = new Doctor(user, specialty, null, 30, null);
		when(doctorRepository.findByIdWithUserAndSpecialty(10L)).thenReturn(Optional.of(doctor));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot("used@example.com", user.getId())).thenReturn(true);

		assertThrows(EmailAlreadyUsedException.class, () -> doctorManagementService.update(10L, new AdminDoctorRequest(
				"Dr Jane",
				"used@example.com",
				null,
				null,
				1L,
				null,
				30,
				null
		)));

		verify(specialtyService, never()).getActiveSpecialty(1L);
	}

	@Test
	void activateAndDeactivateKeepDoctorAndUserStatusSynchronized() {
		Specialty specialty = new Specialty("Cardiology", null);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", null);
		Doctor doctor = new Doctor(user, specialty, null, 30, null);
		when(doctorRepository.findByIdWithUserAndSpecialty(10L)).thenReturn(Optional.of(doctor));

		assertEquals(DoctorStatus.INACTIVE, doctorManagementService.deactivate(10L).status());
		assertEquals(AccountStatus.INACTIVE, user.getAccountStatus());
		assertEquals(DoctorStatus.ACTIVE, doctorManagementService.activate(10L).status());
		assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
	}

	@Test
	void updateWithoutPasswordLeavesPasswordHashUnchangedAndTrimsBlankOptionalFields() {
		Specialty specialty = new Specialty("Cardiology", null);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", "+1555");
		Doctor doctor = new Doctor(user, specialty, "Bio", 30, "Clinic");
		when(doctorRepository.findByIdWithUserAndSpecialty(10L)).thenReturn(Optional.of(doctor));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot("doctor@example.com", user.getId())).thenReturn(false);
		when(specialtyService.getActiveSpecialty(1L)).thenReturn(specialty);

		AdminDoctorResponse response = doctorManagementService.update(10L, new AdminDoctorRequest(
				"Dr Jane",
				"doctor@example.com",
				null,
				" ",
				1L,
				" ",
				30,
				" "
		));

		assertEquals("hash", user.getPasswordHash());
		assertNull(response.phoneNumber());
		assertNull(response.biography());
		assertNull(response.clinicAddress());
	}
}
