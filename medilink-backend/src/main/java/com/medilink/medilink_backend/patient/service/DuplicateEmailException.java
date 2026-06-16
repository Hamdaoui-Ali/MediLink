package com.medilink.medilink_backend.patient.service;

public class DuplicateEmailException extends RuntimeException {

	public DuplicateEmailException(String email) {
		super("An account already exists for email: " + email);
	}
}
