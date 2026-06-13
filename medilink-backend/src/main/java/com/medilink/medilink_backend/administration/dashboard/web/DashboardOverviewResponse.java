package com.medilink.medilink_backend.administration.dashboard.web;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record DashboardOverviewResponse(
		long totalDoctors,
		long totalPatients,
		long totalAppointments,
		long totalSpecialties,
		List<RecentAppointment> recentAppointments
) {

	public record RecentAppointment(
			Long id,
			LocalDate appointmentDate,
			LocalTime startTime,
			String doctorName,
			String patientName,
			String status
	) {}
}
