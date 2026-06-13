package com.medilink.medilink_backend.appointment.web;

import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
		Long id,
		Long doctorId,
		Long patientId,
		LocalDate appointmentDate,
		LocalTime startTime,
		LocalTime endTime,
		AppointmentStatus status,
		String reason,
		String doctorNotes
) {}
