package com.medilink.medilink_backend.doctor.service;

public class DoctorNotFoundException extends RuntimeException {

	public DoctorNotFoundException(Long id) {
		super("Doctor was not found: " + id);
	}
}
