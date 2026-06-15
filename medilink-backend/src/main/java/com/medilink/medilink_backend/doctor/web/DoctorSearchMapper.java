package com.medilink.medilink_backend.doctor.web;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DoctorSearchMapper {

	public DoctorSummaryResponse toSummary(Doctor doctor) {
		return new DoctorSummaryResponse(
				doctor.getId(),
				doctor.getUser().getFullName(),
				doctor.getSpecialtyName(),
				doctor.getBiography(),
				doctor.getConsultationDurationMinutes(),
				doctor.getClinicAddress()
		);
	}

	public DoctorProfileResponse toProfile(Doctor doctor) {
		return new DoctorProfileResponse(
				doctor.getId(),
				doctor.getUser().getId(),
				doctor.getUser().getFullName(),
				doctor.getUser().getEmail(),
				doctor.getUser().getPhoneNumber(),
				doctor.getUser().getAccountStatus().name(),
				doctor.getSpecialtyName(),
				doctor.getBiography(),
				doctor.getConsultationDurationMinutes(),
				doctor.getClinicAddress(),
				doctor.getStatus()
		);
	}

	public List<DoctorSummaryResponse> toSummaryList(List<Doctor> doctors) {
		return doctors.stream().map(this::toSummary).toList();
	}
}
