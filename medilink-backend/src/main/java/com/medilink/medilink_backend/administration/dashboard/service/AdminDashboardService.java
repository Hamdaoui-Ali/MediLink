package com.medilink.medilink_backend.administration.dashboard.service;

import com.medilink.medilink_backend.administration.dashboard.domain.AppointmentDashboard;
import com.medilink.medilink_backend.administration.dashboard.domain.DoctorDashboard;
import com.medilink.medilink_backend.administration.dashboard.repository.AppointmentDashboardRepository;
import com.medilink.medilink_backend.administration.dashboard.repository.DoctorDashboardRepository;
import com.medilink.medilink_backend.administration.dashboard.web.DashboardOverviewResponse;
import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import com.medilink.medilink_backend.administration.repository.SpecialtyRepository;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminDashboardService {

	private final DoctorDashboardRepository doctorDashboardRepository;
	private final AppointmentDashboardRepository appointmentDashboardRepository;
	private final PatientRepository patientRepository;
	private final SpecialtyRepository specialtyRepository;

	public AdminDashboardService(
			DoctorDashboardRepository doctorDashboardRepository,
			AppointmentDashboardRepository appointmentDashboardRepository,
			PatientRepository patientRepository,
			SpecialtyRepository specialtyRepository
	) {
		this.doctorDashboardRepository = doctorDashboardRepository;
		this.appointmentDashboardRepository = appointmentDashboardRepository;
		this.patientRepository = patientRepository;
		this.specialtyRepository = specialtyRepository;
	}

	@Transactional(readOnly = true)
	public DashboardOverviewResponse getOverview() {
		long totalDoctors = doctorDashboardRepository.countByStatus("ACTIVE");
		long totalPatients = patientRepository.count();
		long totalAppointments = appointmentDashboardRepository.count();
		long totalSpecialties = specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE).size();

		List<AppointmentDashboard> recent = appointmentDashboardRepository.findTop5ByOrderByCreatedAtDesc();

		Set<Long> doctorIds = recent.stream()
				.map(AppointmentDashboard::getDoctorId)
				.collect(Collectors.toSet());
		Set<Long> patientIds = recent.stream()
				.map(AppointmentDashboard::getPatientId)
				.collect(Collectors.toSet());

		Map<Long, String> doctorNames = doctorDashboardRepository.findAllById(doctorIds).stream()
				.collect(Collectors.toMap(DoctorDashboard::getId, DoctorDashboard::getFullName));

		Map<Long, String> patientNames = patientRepository.findAllById(patientIds).stream()
				.collect(Collectors.toMap(
						p -> p.getId(),
						p -> p.getUser() != null ? p.getUser().getFullName() : "Unknown"
				));

		List<DashboardOverviewResponse.RecentAppointment> recentAppointments = recent.stream()
				.map(a -> new DashboardOverviewResponse.RecentAppointment(
						a.getId(),
						a.getAppointmentDate(),
						a.getStartTime(),
						doctorNames.getOrDefault(a.getDoctorId(), "Unknown"),
						patientNames.getOrDefault(a.getPatientId(), "Unknown"),
						a.getStatus()
				))
				.toList();

		return new DashboardOverviewResponse(
				totalDoctors,
				totalPatients,
				totalAppointments,
				totalSpecialties,
				recentAppointments
		);
	}
}
