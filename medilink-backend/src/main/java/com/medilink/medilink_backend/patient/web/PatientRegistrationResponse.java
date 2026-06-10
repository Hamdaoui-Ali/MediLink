package com.medilink.medilink_backend.patient.web;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.RoleName;

public record PatientRegistrationResponse(
		Long userId,
		Long patientId,
		String fullName,
		String email,
		String phoneNumber,
		RoleName role,
		AccountStatus accountStatus
) {
}
