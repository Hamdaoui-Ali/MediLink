package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DoctorSearchService {

	private final DoctorRepository doctorRepository;

	public DoctorSearchService(DoctorRepository doctorRepository) {
		this.doctorRepository = doctorRepository;
	}

	@Transactional(readOnly = true)
	public List<Doctor> searchDoctors(Long specialtyId, String name) {
		if (specialtyId != null) {
			return doctorRepository.findBySpecialtyIdAndStatusActive(specialtyId);
		}
		if (name != null && !name.isBlank()) {
			return doctorRepository.findByUserFullNameContainingIgnoreCaseAndStatusActive(name);
		}
		return doctorRepository.findAllActiveWithDetails();
	}

	@Transactional(readOnly = true)
	public Doctor getDoctor(Long doctorId) {
		return doctorRepository.findByIdAndStatusActive(doctorId)
				.orElseThrow(() -> new DoctorNotFoundException(doctorId));
	}
}
