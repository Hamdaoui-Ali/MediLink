package com.medilink.medilink_backend.patient.service;

import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.RoleRepository;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.patient.domain.Patient;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import com.medilink.medilink_backend.patient.web.PatientRegistrationRequest;
import com.medilink.medilink_backend.patient.web.PatientRegistrationResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PatientRegistrationService {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PatientRepository patientRepository;
	private final PasswordEncoder passwordEncoder;

	public PatientRegistrationService(
			RoleRepository roleRepository,
			UserRepository userRepository,
			PatientRepository patientRepository,
			PasswordEncoder passwordEncoder
	) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.patientRepository = patientRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public PatientRegistrationResponse register(PatientRegistrationRequest request) {
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new DuplicateEmailException(email);
		}

		Role patientRole = roleRepository.findByName(RoleName.PATIENT)
				.orElseThrow(() -> new IllegalStateException("PATIENT role is missing from seed data."));
		User user = userRepository.save(new User(
				patientRole,
				request.fullName().trim(),
				email,
				passwordEncoder.encode(request.password()),
				request.phoneNumber().trim()
		));
		Patient patient = patientRepository.save(new Patient(
				user,
				request.dateOfBirth(),
				request.gender(),
				trimToNull(request.address())
		));

		return new PatientRegistrationResponse(
				user.getId(),
				patient.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhoneNumber(),
				user.getRole().getName(),
				user.getAccountStatus()
		);
	}

	String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}
}
