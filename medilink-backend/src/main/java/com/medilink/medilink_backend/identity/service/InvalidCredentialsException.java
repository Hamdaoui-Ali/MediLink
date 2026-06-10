package com.medilink.medilink_backend.identity.service;

public class InvalidCredentialsException extends RuntimeException {

	public InvalidCredentialsException() {
		super("Invalid email or password.");
	}
}
