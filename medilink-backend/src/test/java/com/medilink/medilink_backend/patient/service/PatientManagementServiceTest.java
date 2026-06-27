package com.medilink.medilink_backend.patient.service;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.service.CreateUserCommand;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.UserAccountService;
import com.medilink.medilink_backend.patient.domain.Gender;
import com.medilink.medilink_backend.patient.domain.Patient;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import com.medilink.medilink_backend.patient.web.AdminPatientCreateRequest;
import com.medilink.medilink_backend.patient.web.AdminPatientPasswordRequest;
import com.medilink.medilink_backend.patient.web.AdminPatientResponse;
import com.medilink.medilink_backend.patient.web.AdminPatientUpdateRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
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

class PatientManagementServiceTest {

	private final PatientRepository patientRepository = mock(PatientRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final UserAccountService userAccountService = mock(UserAccountService.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final PatientManagementService service = new PatientManagementService(
			patientRepository,
			userRepository,
			userAccountService,
			passwordEncoder
	);

	@Test
	void listPatientsReturnsAdminResponses() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findAllWithUser()).thenReturn(List.of(patient));

		List<AdminPatientResponse> response = service.listPatients();

		assertEquals(1, response.size());
		assertEquals("Jane Patient", response.getFirst().fullName());
		assertEquals(Gender.FEMALE, response.getFirst().gender());
		assertEquals(AccountStatus.ACTIVE.name(), response.getFirst().accountStatus());
	}

	@Test
	void createPatientCreatesPatientUserAndRecord() {
		User user = user("Jane Patient", "jane@medilink.local");
		when(passwordEncoder.encode("Password123")).thenReturn("encoded-password");
		when(userAccountService.createUser(any(CreateUserCommand.class))).thenReturn(user);
		when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

		AdminPatientCreateRequest request = new AdminPatientCreateRequest(
				"Jane Patient",
				"jane@medilink.local",
				"Password123",
				"+15551234567",
				LocalDate.of(1990, 1, 15),
				Gender.FEMALE,
				"  123 Patient St  "
		);

		AdminPatientResponse response = service.createPatient(request);

		ArgumentCaptor<CreateUserCommand> commandCaptor = ArgumentCaptor.forClass(CreateUserCommand.class);
		verify(userAccountService).createUser(commandCaptor.capture());
		assertEquals(RoleName.PATIENT, commandCaptor.getValue().roleName());
		assertEquals("encoded-password", commandCaptor.getValue().passwordHash());
		assertEquals("Jane Patient", response.fullName());
		assertEquals("123 Patient St", response.address());
	}

	@Test
	void updatePatientUpdatesUserAndPatientFields() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findByIdWithUser(1L)).thenReturn(Optional.of(patient));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(eq("updated@medilink.local"), any())).thenReturn(false);

		AdminPatientUpdateRequest request = new AdminPatientUpdateRequest(
				"  Jane Updated  ",
				"  UPDATED@MEDILINK.LOCAL  ",
				"  +15557654321  ",
				LocalDate.of(1992, 2, 20),
				Gender.OTHER,
				"  Updated address  "
		);

		AdminPatientResponse response = service.updatePatient(1L, request);

		assertEquals("Jane Updated", response.fullName());
		assertEquals("updated@medilink.local", response.email());
		assertEquals("+15557654321", response.phoneNumber());
		assertEquals(Gender.OTHER, response.gender());
		assertEquals("Updated address", response.address());
	}

	@Test
	void updatePatientRejectsDuplicateEmail() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findByIdWithUser(1L)).thenReturn(Optional.of(patient));
		when(userRepository.existsByEmailIgnoreCaseAndIdNot(eq("taken@medilink.local"), any())).thenReturn(true);

		AdminPatientUpdateRequest request = new AdminPatientUpdateRequest(
				"Jane Updated",
				"taken@medilink.local",
				null,
				null,
				Gender.UNSPECIFIED,
				null
		);

		assertThrows(EmailAlreadyUsedException.class, () -> service.updatePatient(1L, request));
	}

	@Test
	void activateAndDeactivateKeepUserStatusInSync() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findByIdWithUser(1L)).thenReturn(Optional.of(patient));

		AdminPatientResponse deactivated = service.deactivatePatient(1L);
		assertEquals(AccountStatus.INACTIVE.name(), deactivated.accountStatus());

		AdminPatientResponse activated = service.activatePatient(1L);
		assertEquals(AccountStatus.ACTIVE.name(), activated.accountStatus());
	}

	@Test
	void resetPasswordUpdatesPatientUserPasswordHash() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findByIdWithUser(1L)).thenReturn(Optional.of(patient));
		when(passwordEncoder.encode("NewPassword123")).thenReturn("new-hash");

		AdminPatientResponse response = service.resetPassword(1L, new AdminPatientPasswordRequest("NewPassword123"));

		assertEquals("Jane Patient", response.fullName());
		assertEquals("new-hash", patient.getUser().getPasswordHash());
	}

	@Test
	void blankOptionalFieldsArePersistedAsNull() {
		Patient patient = patient("Jane Patient", "jane@medilink.local");
		when(patientRepository.findByIdWithUser(1L)).thenReturn(Optional.of(patient));

		AdminPatientUpdateRequest request = new AdminPatientUpdateRequest(
				"Jane Patient",
				"jane@medilink.local",
				" ",
				null,
				Gender.UNSPECIFIED,
				" "
		);

		AdminPatientResponse response = service.updatePatient(1L, request);

		assertNull(response.phoneNumber());
		assertNull(response.address());
	}

	@Test
	void updateThrowsWhenPatientDoesNotExist() {
		when(patientRepository.findByIdWithUser(404L)).thenReturn(Optional.empty());

		AdminPatientUpdateRequest request = new AdminPatientUpdateRequest(
				"Missing",
				"missing@medilink.local",
				null,
				null,
				Gender.UNSPECIFIED,
				null
		);

		assertThrows(PatientNotFoundException.class, () -> service.updatePatient(404L, request));
	}

	private Patient patient(String fullName, String email) {
		return new Patient(
				user(fullName, email),
				LocalDate.of(1990, 1, 15),
				Gender.FEMALE,
				"123 Patient St"
		);
	}

	private User user(String fullName, String email) {
		return new User(
				new Role(RoleName.PATIENT, "Patient"),
				fullName,
				email,
				"hash",
				"+15551234567"
		);
	}
}
