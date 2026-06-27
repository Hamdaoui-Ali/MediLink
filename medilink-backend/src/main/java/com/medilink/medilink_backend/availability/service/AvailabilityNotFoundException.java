package com.medilink.medilink_backend.availability.service;

public class AvailabilityNotFoundException extends RuntimeException {

	public AvailabilityNotFoundException(Long availabilityId) {
		super("Availability range was not found: " + availabilityId);
	}
}
