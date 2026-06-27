package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.AdminDoctorCreateRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorPasswordRequest;
import com.medilink.medilink_backend.doctor.web.AdminDoctorResponse;
import com.medilink.medilink_backend.doctor.web.AdminDoctorUpdateRequest;
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
	private final UserRepository userRepository;
	private final UserAccountService userAccountService;
	private final SpecialtyService specialtyService;
	private final PasswordEncoder passwordEncoder;

	public DoctorManagementService(
			DoctorRepository doctorRepository,
			UserRepository userRepository,
			UserAccountService userAccountService,
			SpecialtyService specialtyService,
			PasswordEncoder passwordEncoder
	) {
		this.doctorRepository = doctorRepository;
		this.userRepository = userRepository;
		this.userAccountService = userAccountService;
		this.specialtyService = specialtyService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public List<AdminDoctorResponse> listDoctors() {
		return doctorRepository.findAllWithDetails().stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public AdminDoctorResponse createDoctor(AdminDoctorCreateRequest request) {
		Specialty specialty = specialtyService.getActiveSpecialty(request.specialtyId());
		User user = userAccountService.createUser(new CreateUserCommand(
				RoleName.DOCTOR,
				request.fullName(),
				request.email(),
				passwordEncoder.encode(request.password()),
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
	public AdminDoctorResponse updateDoctor(Long doctorId, AdminDoctorUpdateRequest request) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new DoctorNotFoundException(doctorId));
		Specialty specialty = specialtyService.getActiveSpecialty(request.specialtyId());
		User user = doctor.getUser();
		String email = request.email().trim().toLowerCase();

		if (userRepository.existsByEmailIgnoreCaseAndIdNot(email, user.getId())) {
			throw new EmailAlreadyUsedException(email);
		}

		user.updateIdentity(request.fullName().trim(), email, trimToNull(request.phoneNumber()));
		doctor.updateAdminFields(
				specialty,
				trimToNull(request.biography()),
				request.consultationDurationMinutes(),
				trimToNull(request.clinicAddress())
		);

		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse activateDoctor(Long doctorId) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new DoctorNotFoundException(doctorId));
		doctor.activate();
		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse deactivateDoctor(Long doctorId) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new DoctorNotFoundException(doctorId));
		doctor.deactivate();
		return toResponse(doctor);
	}

	@Transactional
	public AdminDoctorResponse resetPassword(Long doctorId, AdminDoctorPasswordRequest request) {
		Doctor doctor = doctorRepository.findById(doctorId)
				.orElseThrow(() -> new DoctorNotFoundException(doctorId));
		doctor.getUser().updatePasswordHash(passwordEncoder.encode(request.password()));
		return toResponse(doctor);
	}

	private AdminDoctorResponse toResponse(Doctor doctor) {
		User user = doctor.getUser();
		return new AdminDoctorResponse(
				doctor.getId(),
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhoneNumber(),
				user.getAccountStatus().name(),
				doctor.getSpecialty().getId(),
				doctor.getSpecialtyName(),
				doctor.getBiography(),
				doctor.getConsultationDurationMinutes(),
				doctor.getClinicAddress(),
				doctor.getStatus()
		);
	}

	private String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		return value.trim();
	}
}
