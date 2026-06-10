package com.medilink.medilink_backend.identity.service;

public class UserNotFoundException extends RuntimeException {

	public UserNotFoundException(Long id) {
		super("User was not found for id: " + id);
	}

	public UserNotFoundException(String email) {
		super("User was not found for email: " + email);
	}
}
