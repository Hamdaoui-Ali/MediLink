package com.medilink.medilink_backend.doctor.service;

public class AvailabilityNotFoundException extends RuntimeException {

	public AvailabilityNotFoundException(Long id) {
		super("Availability slot was not found: " + id);
	}
}
