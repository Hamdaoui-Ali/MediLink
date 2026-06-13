package com.medilink.medilink_backend.administration.dashboard.service;

import com.medilink.medilink_backend.administration.dashboard.domain.AppointmentDashboard;
import com.medilink.medilink_backend.administration.dashboard.domain.DoctorDashboard;
import com.medilink.medilink_backend.administration.dashboard.repository.AppointmentDashboardRepository;
import com.medilink.medilink_backend.administration.dashboard.repository.DoctorDashboardRepository;
import com.medilink.medilink_backend.administration.dashboard.web.DashboardOverviewResponse;
import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import com.medilink.medilink_backend.administration.repository.SpecialtyRepository;
import com.medilink.medilink_backend.patient.domain.Patient;
import com.medilink.medilink_backend.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminDashboardServiceTest {

	private final DoctorDashboardRepository doctorDashboardRepository = mock(DoctorDashboardRepository.class);
	private final AppointmentDashboardRepository appointmentDashboardRepository = mock(AppointmentDashboardRepository.class);
	private final PatientRepository patientRepository = mock(PatientRepository.class);
	private final SpecialtyRepository specialtyRepository = mock(SpecialtyRepository.class);

	private final AdminDashboardService adminDashboardService = new AdminDashboardService(
			doctorDashboardRepository,
			appointmentDashboardRepository,
			patientRepository,
			specialtyRepository
	);

	@Test
	void getOverviewReturnsTotalCountsForAllEntities() {
		when(doctorDashboardRepository.countByStatus("ACTIVE")).thenReturn(3L);
		when(patientRepository.count()).thenReturn(10L);
		when(appointmentDashboardRepository.count()).thenReturn(5L);
		when(specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE))
				.thenReturn(List.of(new Specialty("Cardiology", null), new Specialty("Dermatology", null)));
		when(appointmentDashboardRepository.findTop5ByOrderByCreatedAtDesc())
				.thenReturn(Collections.emptyList());

		DashboardOverviewResponse overview = adminDashboardService.getOverview();

		assertEquals(3L, overview.totalDoctors());
		assertEquals(10L, overview.totalPatients());
		assertEquals(5L, overview.totalAppointments());
		assertEquals(2L, overview.totalSpecialties());
		assertTrue(overview.recentAppointments().isEmpty());
	}

	@Test
	void getOverviewIncludesRecentAppointmentsWithDoctorAndPatientNames() {
		when(doctorDashboardRepository.countByStatus("ACTIVE")).thenReturn(1L);
		when(patientRepository.count()).thenReturn(1L);
		when(appointmentDashboardRepository.count()).thenReturn(1L);
		when(specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE))
				.thenReturn(Collections.emptyList());

		AppointmentDashboard appointment = mock(AppointmentDashboard.class);
		when(appointment.getId()).thenReturn(100L);
		when(appointment.getDoctorId()).thenReturn(1L);
		when(appointment.getPatientId()).thenReturn(2L);
		when(appointment.getAppointmentDate()).thenReturn(java.time.LocalDate.of(2026, 6, 15));
		when(appointment.getStartTime()).thenReturn(java.time.LocalTime.of(10, 0));
		when(appointment.getStatus()).thenReturn("CONFIRMED");

		when(appointmentDashboardRepository.findTop5ByOrderByCreatedAtDesc())
				.thenReturn(List.of(appointment));

		DoctorDashboard doctor = mock(DoctorDashboard.class);
		when(doctor.getId()).thenReturn(1L);
		when(doctor.getFullName()).thenReturn("Dr. Smith");

		when(doctorDashboardRepository.findAllById(Set.of(1L)))
				.thenReturn(List.of(doctor));

		Patient patient = mock(Patient.class);
		when(patient.getId()).thenReturn(2L);
		when(patient.getUser()).thenReturn(null);

		when(patientRepository.findAllById(Set.of(2L)))
				.thenReturn(List.of(patient));

		DashboardOverviewResponse overview = adminDashboardService.getOverview();

		assertEquals(1, overview.recentAppointments().size());
		DashboardOverviewResponse.RecentAppointment recent = overview.recentAppointments().getFirst();
		assertEquals(100L, recent.id());
		assertEquals(java.time.LocalDate.of(2026, 6, 15), recent.appointmentDate());
		assertEquals(java.time.LocalTime.of(10, 0), recent.startTime());
		assertEquals("Dr. Smith", recent.doctorName());
		assertEquals("Unknown", recent.patientName());
		assertEquals("CONFIRMED", recent.status());
	}

	@Test
	void getOverviewHandlesEmptyDashboardGracefully() {
		when(doctorDashboardRepository.countByStatus("ACTIVE")).thenReturn(0L);
		when(patientRepository.count()).thenReturn(0L);
		when(appointmentDashboardRepository.count()).thenReturn(0L);
		when(specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE))
				.thenReturn(Collections.emptyList());
		when(appointmentDashboardRepository.findTop5ByOrderByCreatedAtDesc())
				.thenReturn(Collections.emptyList());

		DashboardOverviewResponse overview = adminDashboardService.getOverview();

		assertNotNull(overview);
		assertEquals(0L, overview.totalDoctors());
		assertEquals(0L, overview.totalPatients());
		assertEquals(0L, overview.totalAppointments());
		assertEquals(0L, overview.totalSpecialties());
		assertTrue(overview.recentAppointments().isEmpty());
	}
}
