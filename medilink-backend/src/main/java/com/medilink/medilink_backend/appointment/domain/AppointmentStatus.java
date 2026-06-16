package com.medilink.medilink_backend.appointment.domain;

public enum AppointmentStatus {
	CONFIRMED,
	CANCELLED,
	COMPLETED,
	MISSED,
	RESCHEDULED;

	public boolean isTerminal() {
		return this == COMPLETED || this == CANCELLED || this == MISSED;
	}

	public boolean canTransitionTo(AppointmentStatus target) {
		if (this == target) return false;
		if (this == CONFIRMED) return target == COMPLETED || target == CANCELLED || target == MISSED || target == RESCHEDULED;
		if (this == RESCHEDULED) return target == CONFIRMED || target == COMPLETED || target == CANCELLED || target == MISSED;
		return false;
	}
}
