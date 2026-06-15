package com.medilink.medilink_backend.appointment.service;

public class PatientNotFoundException extends RuntimeException {

	public PatientNotFoundException(Long userId) {
		super("No patient account found for user ID: " + userId);
	}
}
