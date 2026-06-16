package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.DoctorProfileResponse;
import com.medilink.medilink_backend.doctor.web.DoctorProfileUpdateRequest;
import com.medilink.medilink_backend.identity.domain.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DoctorProfileService {

	private final DoctorRepository doctorRepository;

	public DoctorProfileService(DoctorRepository doctorRepository) {
		this.doctorRepository = doctorRepository;
	}

	@Transactional(readOnly = true)
	public DoctorProfileResponse getProfile(Long userId) {
		Doctor doctor = doctorRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorNotFoundException(userId));
		return toResponse(doctor);
	}

	@Transactional
	public DoctorProfileResponse updateProfile(Long userId, DoctorProfileUpdateRequest request) {
		Doctor doctor = doctorRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorNotFoundException(userId));

		String biography = request.biography();
		String clinicAddress = request.clinicAddress();
		int consultationDuration = request.consultationDurationMinutes() != null
				? request.consultationDurationMinutes()
				: doctor.getConsultationDurationMinutes();

		doctor.updateProfile(
				biography != null ? biography.trim() : doctor.getBiography(),
				clinicAddress != null ? clinicAddress.trim() : doctor.getClinicAddress(),
				consultationDuration
		);

		if (request.phoneNumber() != null) {
			doctor.getUser().updatePhoneNumber(request.phoneNumber().trim());
		}

		return toResponse(doctor);
	}

	private DoctorProfileResponse toResponse(Doctor doctor) {
		User user = doctor.getUser();
		return new DoctorProfileResponse(
				doctor.getId(),
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getPhoneNumber(),
				user.getAccountStatus().name(),
				doctor.getSpecialtyName(),
				doctor.getBiography(),
				doctor.getConsultationDurationMinutes(),
				doctor.getClinicAddress(),
				doctor.getStatus()
		);
	}
}
