package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.AdminDoctorRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorResponse;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.service.CreateUserCommand;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.UserAccountService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorManagementService {

	private final DoctorRepository doctorRepository;
	private final SpecialtyService specialtyService;
	private final UserAccountService userAccountService;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DoctorManagementService(
			DoctorRepository doctorRepository,
			SpecialtyService specialtyService,
			UserAccountService userAccountService,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder
	) {
		this.doctorRepository = doctorRepository;
		this.specialtyService = specialtyService;
		this.userAccountService = userAccountService;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<AdminDoctorResponse> list() {
		return doctorRepository.findAllWithUserAndSpecialtyOrderByUserFullNameAsc().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public AdminDoctorResponse create(AdminDoctorRequest request) {
		String password = requirePassword(request.password());
		Specialty specialty = specialtyService.getActiveSpecialty(request.specialtyId());
		User user = userAccountService.createUser(new CreateUserCommand(
				RoleName.DOCTOR,
				request.fullName(),
				request.email(),
				passwordEncoder.encode(password),
				request.phoneNumber()
		));

		Doctor doctor = doctorRepository.save(new Doctor(
				user,
				specialty,
				trimToNull(request.biography()),
				request.consultationDurationMinutes(),
				trimToNull(request.clinicAddress())
		));

		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse update(Long id, AdminDoctorRequest request) {
		Doctor doctor = findById(id);
		User user = doctor.getUser();
		String email = normalizeEmail(request.email());

		if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
			throw new EmailAlreadyUsedException(email);
		}

		user.updateProfile(request.fullName().trim(), email, trimToNull(request.phoneNumber()));
		updatePasswordIfPresent(user, request.password());

		Specialty specialty = specialtyService.getActiveSpecialty(request.specialtyId());
		doctor.update(
				specialty,
				trimToNull(request.biography()),
				request.consultationDurationMinutes(),
				trimToNull(request.clinicAddress())
		);

		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse activate(Long id) {
		Doctor doctor = findById(id);
		doctor.activate();
		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse deactivate(Long id) {
		Doctor doctor = findById(id);
		doctor.deactivate();
		return toResponse(doctor);
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

	private Doctor findById(Long id) {
		return doctorRepository.findByIdWithUserAndSpecialty(id)
				.orElseThrow(() -> new DoctorNotFoundException(id));
	}

	private String requirePassword(String password) {
		if (password == null || password.isBlank()) {
			throw new InvalidDoctorRequestException("Password is required when creating a doctor account.");
		}

		return password.trim();
	}

	private void updatePasswordIfPresent(User user, String password) {
		if (password != null && !password.isBlank()) {
			user.updatePasswordHash(passwordEncoder.encode(password.trim()));
		}
	}

	private AdminDoctorResponse toResponse(Doctor doctor) {
		User user = doctor.getUser();
		Specialty specialty = doctor.getSpecialty();

		return new AdminDoctorResponse(
				doctor.getId(),
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhoneNumber(),
				specialty.getId(),
				specialty.getName(),
				doctor.getBiography(),
				doctor.getConsultationDurationMinutes(),
				doctor.getClinicAddress(),
				doctor.getStatus()
		);
	}
}
