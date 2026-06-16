package com.medilink.medilink_backend.administration.service;

public class DuplicateSpecialtyNameException extends RuntimeException {

	public DuplicateSpecialtyNameException(String name) {
		super("A specialty already exists with name: " + name);
	}
}
