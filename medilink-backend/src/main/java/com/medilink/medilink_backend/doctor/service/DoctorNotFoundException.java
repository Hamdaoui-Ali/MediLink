package com.medilink.medilink_backend.doctor.service;

public class DoctorNotFoundException extends RuntimeException {

	public DoctorNotFoundException(Long userId) {
		super("No active doctor account found for user ID: " + userId);
	}
}
