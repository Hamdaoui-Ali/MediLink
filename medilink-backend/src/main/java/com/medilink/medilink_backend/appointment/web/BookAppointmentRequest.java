package com.medilink.medilink_backend.appointment.web;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record BookAppointmentRequest(
		@NotNull Long doctorId,
		@NotNull @FutureOrPresent LocalDate appointmentDate,
		@NotNull LocalTime startTime,
		@NotBlank String reason
) {}
