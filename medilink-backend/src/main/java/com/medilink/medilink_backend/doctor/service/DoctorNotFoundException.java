package com.medilink.medilink_backend.doctor.service;

public class DoctorNotFoundException extends RuntimeException {

	public DoctorNotFoundException(Long userId) {
		super("Doctor profile not found for user id: " + userId);
	}
}
