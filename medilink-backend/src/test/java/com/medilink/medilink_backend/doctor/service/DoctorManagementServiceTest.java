package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.AdminDoctorCreateRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorPasswordRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorResponse;
import com.medilink.medilink_backend.doctor.web.AdminDoctorUpdateRequest;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DoctorManagementServiceTest {

	private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserAccountService userAccountService = mock(UserAccountService.class);
	private final SpecialtyService specialtyService = mock(SpecialtyService.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final DoctorManagementService service = new DoctorManagementService(
			doctorRepository,
			userRepository,
			userAccountService,
			specialtyService,
			passwordEncoder
	);

	@Test
	void listDoctorsReturnsAdminResponses() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		when(doctorRepository.findAllWithDetails()).thenReturn(List.of(doctor));

		List<AdminDoctorResponse> response = service.listDoctors();

		assertEquals(1, response.size());
		assertEquals("Dr. Jane", response.getFirst().fullName());
		assertEquals("Cardiology", response.getFirst().specialtyName());
		assertEquals("ACTIVE", response.getFirst().status());
	}

	@Test
	void createDoctorCreatesDoctorUserAndProfile() {
		Specialty specialty = new Specialty("Cardiology", "Heart care");
		User user = user("Dr. Jane", "jane@medilink.local");
		when(specialtyService.getActiveSpecialty(10L)).thenReturn(specialty);
		when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
		when(userAccountService.createUser(any(CreateUserCommand.class))).thenReturn(user);
		when(doctorRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AdminDoctorCreateRequest request = new AdminDoctorCreateRequest(
				"Dr. Jane",
				"jane@medilink.local",
				"Password123",
				"+15551234567",
				10L,
				"  Experienced cardiologist  ",
				30,
				"  Main clinic  "
		);

		AdminDoctorResponse response = service.createDoctor(request);

		ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
		verify(userAccountService).createUser(commandCaptor.capture());
		assertEquals(RoleName.DOCTOR, commandCaptor.getValue().roleName());
		assertEquals("encoded-password", commandCaptor.getValue().passwordHash());
		assertEquals("Dr. Jane", response.fullName());
		assertEquals("Experienced cardiologist", response.biography());
		assertEquals("Main clinic", response.clinicAddress());
	}

	@Test
	void updateDoctorUpdatesUserAndDoctorFields() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		Specialty neurology = new Specialty("Neurology", "Brain care");
		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(specialtyService.getActiveSpecialty(20L)).thenReturn(neurology);
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(eq("updated@medilink.local"), any())).thenReturn(false);

		AdminDoctorUpdateRequest request = new AdminDoctorUpdateRequest(
				"  Dr. Updated  ",
				"  UPDATED@MEDILINK.LOCAL  ",
				"  +15557654321  ",
				20L,
				"  Updated bio  ",
				45,
				"  Updated clinic  "
		);

		AdminDoctorResponse response = service.updateDoctor(1L, request);

		assertEquals("Dr. Updated", response.fullName());
		assertEquals("updated@medilink.local", response.email());
		assertEquals("+15557654321", response.phoneNumber());
		assertEquals("Neurology", response.specialtyName());
		assertEquals("Updated bio", response.biography());
		assertEquals(45, response.consultationDurationMinutes());
		assertEquals("Updated clinic", response.clinicAddress());
	}

	@Test
	void updateDoctorRejectsDuplicateEmail() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(specialtyService.getActiveSpecialty(20L)).thenReturn(new Specialty("Neurology", "Brain care"));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(eq("taken@medilink.local"), any())).thenReturn(true);

		AdminDoctorUpdateRequest request = new AdminDoctorUpdateRequest(
				"Dr. Updated",
				"taken@medilink.local",
				null,
				20L,
				null,
				30,
				null
		);

		assertThrows(EmailAlreadyUsedException.class, () -> service.updateDoctor(1L, request));
	}

	@Test
	void activateAndDeactivateKeepUserStatusInSync() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

		AdminDoctorResponse deactivated = service.deactivateDoctor(1L);
		assertEquals("INACTIVE", deactivated.status());
		assertEquals(AccountStatus.INACTIVE.name(), deactivated.accountStatus());

		AdminDoctorResponse activated = service.activateDoctor(1L);
		assertEquals("ACTIVE", activated.status());
		assertEquals(AccountStatus.ACTIVE.name(), activated.accountStatus());
	}

	@Test
	void updateThrowsWhenDoctorDoesNotExist() {
		when(doctorRepository.findById(404L)).thenReturn(Optional.empty());

		AdminDoctorUpdateRequest request = new AdminDoctorUpdateRequest(
				"Dr. Missing",
				"missing@medilink.local",
				null,
				1L,
				null,
				30,
				null
		);

		assertThrows(DoctorNotFoundException.class, () -> service.updateDoctor(404L, request));
	}

	@Test
	void blankOptionalFieldsArePersistedAsNull() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(specialtyService.getActiveSpecialty(10L)).thenReturn(new Specialty("Cardiology", "Heart care"));

		AdminDoctorUpdateRequest request = new AdminDoctorUpdateRequest(
				"Dr. Jane",
				"jane@medilink.local",
				" ",
				10L,
				" ",
				30,
				" "
		);

		AdminDoctorResponse response = service.updateDoctor(1L, request);

		assertNull(response.phoneNumber());
		assertNull(response.biography());
		assertNull(response.clinicAddress());
	}

	@Test
	void resetPasswordUpdatesDoctorUserPasswordHash() {
		Doctor doctor = doctor("Dr. Jane", "jane@medilink.local", "Cardiology");
		when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
		when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");

		AdminDoctorResponse response = service.resetPassword(1L, new AdminDoctorPasswordRequest("NewPassword123"));

		assertEquals("Dr. Jane", response.fullName());
		assertEquals("new-hash", doctor.getUser().getPasswordHash());
	}

	@Test
	void resetPasswordThrowsWhenDoctorDoesNotExist() {
		when(doctorRepository.findById(404L)).thenReturn(Optional.empty());

		assertThrows(DoctorNotFoundException.class,
				() -> service.resetPassword(404L, new AdminDoctorPasswordRequest("NewPassword123")));
	}

	private Doctor doctor(String fullName, String email, String specialtyName) {
		return new Doctor(
				user(fullName, email),
				new Specialty(specialtyName, specialtyName + " description"),
				"Bio",
				30,
				"Clinic"
		);
	}

	private User user(String fullName, String email) {
		return new User(
				new Role(RoleName.DOCTOR, "Doctor"),
				fullName,
				email,
				"hash",
				"+15551234567"
		);
	}
}
