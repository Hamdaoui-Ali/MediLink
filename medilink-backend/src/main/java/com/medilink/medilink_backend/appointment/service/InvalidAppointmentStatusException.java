package com.medilink.medilink_backend.appointment.service;

public class InvalidAppointmentStatusException extends RuntimeException {

	public InvalidAppointmentStatusException(String message) {
		super(message);
	}
}
