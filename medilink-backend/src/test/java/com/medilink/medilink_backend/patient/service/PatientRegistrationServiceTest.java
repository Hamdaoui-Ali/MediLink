package com.medilink.medilink_backend.patient.service;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.RoleRepository;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.patient.domain.Gender;
import com.medilink.medilink_backend.patient.domain.Patient;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import com.medilink.medilink_backend.patient.web.PatientRegistrationRequest;
import com.medilink.medilink_backend.patient.web.PatientRegistrationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PatientRegistrationServiceTest {

	private final RoleRepository roleRepository = mock(RoleRepository.class);
	private final UserRepository userRepository = mock(UserRepository.class);
	private final PatientRepository patientRepository = mock(PatientRepository.class);
	private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
	private final PatientRegistrationService service = new PatientRegistrationService(
			roleRepository,
			userRepository,
			patientRepository,
			passwordEncoder
	);

	@Test
	void registerCreatesActivePatientUserAndProfile() {
		Role patientRole = new Role(RoleName.PATIENT, "Patient account");
		PatientRegistrationRequest request = new PatientRegistrationRequest(
				"  Jane Patient  ",
				"  JANE@Example.COM ",
				"Patient@123",
				"  +15551234567 ",
				LocalDate.of(1990, 3, 5),
				Gender.FEMALE,
				"  100 Care Street "
		);
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
		when(passwordEncoder.encode("Patient@123")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

		PatientRegistrationResponse response = service.register(request);

		assertEquals("Jane Patient", response.fullName());
		assertEquals("jane@example.com", response.email());
		assertEquals("+15551234567", response.phoneNumber());
		assertEquals(RoleName.PATIENT, response.role());
		assertEquals(AccountStatus.ACTIVE, response.accountStatus());
		verify(userRepository).save(any(User.class));
		verify(patientRepository).save(any(Patient.class));
	}

	@Test
	void registerRejectsDuplicateEmailBeforeCreatingRecords() {
		PatientRegistrationRequest request = new PatientRegistrationRequest(
				"Jane Patient",
				"jane@example.com",
				"Patient@123",
				"+15551234567",
				null,
				null,
				null
		);
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(true);

		assertThrows(DuplicateEmailException.class, () -> service.register(request));

		verify(roleRepository, never()).findByName(RoleName.PATIENT);
		verify(userRepository, never()).save(any(User.class));
		verify(patientRepository, never()).save(any(Patient.class));
	}

	@Test
	void registerStoresHashedPasswordInsteadOfRawPassword() {
		Role patientRole = new Role(RoleName.PATIENT, "Patient account");
		PatientRegistrationRequest request = new PatientRegistrationRequest(
				"Jane Patient",
				"jane@example.com",
				"Patient@123",
				"+15551234567",
				null,
				null,
				null
		);
		when(userRepository.existsByEmailIgnoreCase("jane@example.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.PATIENT)).thenReturn(Optional.of(patientRole));
		when(passwordEncoder.encode("Patient@123")).thenReturn("hashed-password");
		when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(patientRepository.save(any(Patient.class))).thenAnswer(invocation -> invocation.getArgument(0));

		service.register(request);

		verify(userRepository).save(org.mockito.ArgumentMatchers.argThat(user ->
				"hashed-password".equals(user.getPasswordHash())
						&& !"Patient@123".equals(user.getPasswordHash())
		));
	}

	@Test
	void normalizeEmailTrimsAndLowercasesEmail() {
		assertEquals("jane@example.com", service.normalizeEmail("  JANE@Example.COM "));
	}

	@Test
	void trimToNullTrimsValueAndConvertsBlankToNull() {
		assertEquals("100 Care Street", service.trimToNull("  100 Care Street "));
		assertNull(service.trimToNull("   "));
		assertNull(service.trimToNull(null));
	}
}
