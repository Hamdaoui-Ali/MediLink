package com.medilink.medilink_backend.appointment.service;

public class DoctorRefNotFoundException extends RuntimeException {

	public DoctorRefNotFoundException(Long userId) {
		super("No active doctor account found for user ID: " + userId);
	}
}
