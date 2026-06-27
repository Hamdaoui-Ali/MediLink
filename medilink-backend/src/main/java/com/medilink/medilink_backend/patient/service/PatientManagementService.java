package com.medilink.medilink_backend.patient.service;

import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.service.CreateUserCommand;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.UserAccountService;
import com.medilink.medilink_backend.patient.domain.Patient;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import com.medilink.medilink_backend.patient.web.AdminPatientCreateRequest;
import com.medilink.medilink_backend.patient.web.AdminPatientPasswordRequest;
import com.medilink.medilink_backend.patient.web.AdminPatientResponse;
import com.medilink.medilink_backend.patient.web.AdminPatientUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PatientManagementService {

	private final PatientRepository patientRepository;
	private final UserRepository userRepository;
	private final UserAccountService userAccountService;
	private final PasswordEncoder passwordEncoder;

	public PatientManagementService(
			PatientRepository patientRepository,
			UserRepository userRepository,
			UserAccountService userAccountService,
			PasswordEncoder passwordEncoder
	) {
		this.patientRepository = patientRepository;
		this.userRepository = userRepository;
		this.userAccountService = userAccountService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<AdminPatientResponse> listPatients() {
		return patientRepository.findAllWithUser().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public AdminPatientResponse createPatient(AdminPatientCreateRequest request) {
		User user = userAccountService.createUser(new CreateUserCommand(
				RoleName.PATIENT,
				request.fullName(),
				request.email(),
				passwordEncoder.encode(request.password()),
				request.phoneNumber()
		));

		Patient patient = patientRepository.save(new Patient(
				user,
				request.dateOfBirth(),
				request.gender(),
				trimToNull(request.address())
		));

		return toResponse(patient);
	}

	@Transactional
	public AdminPatientResponse updatePatient(Long patientId, AdminPatientUpdateRequest request) {
		Patient patient = findPatient(patientId);
		User user = patient.getUser();
		String email = request.email().trim().toLowerCase();

		if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
			throw new EmailAlreadyUsedException(email);
		}

		user.updateIdentity(request.fullName().trim(), email, trimToNull(request.phoneNumber()));
		patient.update(request.dateOfBirth(), request.gender(), trimToNull(request.address()));
		return toResponse(patient);
	}

	@Transactional
	public AdminPatientResponse activatePatient(Long patientId) {
		Patient patient = findPatient(patientId);
		patient.getUser().activate();
		return toResponse(patient);
	}

	@Transactional
	public AdminPatientResponse deactivatePatient(Long patientId) {
		Patient patient = findPatient(patientId);
		patient.getUser().deactivate();
		return toResponse(patient);
	}

	@Transactional
	public AdminPatientResponse resetPassword(Long patientId, AdminPatientPasswordRequest request) {
		Patient patient = findPatient(patientId);
		patient.getUser().updatePasswordHash(passwordEncoder.encode(request.password()));
		return toResponse(patient);
	}

	private Patient findPatient(Long patientId) {
		return patientRepository.findByIdWithUser(patientId)
				.orElseThrow(() -> new PatientNotFoundException(patientId));
	}

	private AdminPatientResponse toResponse(Patient patient) {
		User user = patient.getUser();
		return new AdminPatientResponse(
				patient.getId(),
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhoneNumber(),
				user.getAccountStatus().name(),
				patient.getDateOfBirth(),
				patient.getGender(),
				patient.getAddress()
		);
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
