package com.medilink.medilink_backend.identity.service;

public class EmailAlreadyUsedException extends RuntimeException {

	public EmailAlreadyUsedException(String email) {
		super("An account already exists for email: " + email);
	}
}
