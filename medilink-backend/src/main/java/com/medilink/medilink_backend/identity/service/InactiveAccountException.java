package com.medilink.medilink_backend.identity.service;

public class InactiveAccountException extends RuntimeException {

	public InactiveAccountException() {
		super("Account is not active.");
	}
}
