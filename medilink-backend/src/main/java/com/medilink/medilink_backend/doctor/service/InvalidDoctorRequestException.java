package com.medilink.medilink_backend.doctor.service;

public class InvalidDoctorRequestException extends RuntimeException {

	public InvalidDoctorRequestException(String message) {
		super(message);
	}
}
