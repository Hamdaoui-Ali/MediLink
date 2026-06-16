package com.medilink.medilink_backend.appointment.service;

public class AppointmentNotFoundException extends RuntimeException {

	public AppointmentNotFoundException(Long id) {
		super("Appointment not found: " + id);
	}
}
