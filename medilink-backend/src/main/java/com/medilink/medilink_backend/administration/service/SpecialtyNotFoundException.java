package com.medilink.medilink_backend.administration.service;

public class SpecialtyNotFoundException extends RuntimeException {

	public SpecialtyNotFoundException(Long id) {
		super("Specialty was not found for id: " + id);
	}
}
