package com.medilink.medilink_backend.appointment.service;

public class SlotUnavailableException extends RuntimeException {

	public SlotUnavailableException(String message) {
		super(message);
	}
}
