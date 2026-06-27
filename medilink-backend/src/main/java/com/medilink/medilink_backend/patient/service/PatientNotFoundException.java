package com.medilink.medilink_backend.patient.service;

public class PatientNotFoundException extends RuntimeException {

	public PatientNotFoundException(Long id) {
		super("Patient not found with id: " + id);
	}
}
