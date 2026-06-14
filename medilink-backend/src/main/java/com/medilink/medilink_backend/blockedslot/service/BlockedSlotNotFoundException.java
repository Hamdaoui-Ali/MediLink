package com.medilink.medilink_backend.blockedslot.service;

public class BlockedSlotNotFoundException extends RuntimeException {

	public BlockedSlotNotFoundException(Long id) {
		super("Blocked slot not found with id: " + id);
	}
}
