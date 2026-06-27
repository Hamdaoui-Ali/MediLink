package com.medilink.medilink_backend.doctor.domain;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DoctorTest {

	@Test
	void updateChangesEditableDoctorProfileFields() {
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", null);
		Specialty oldSpecialty = new Specialty("General Medicine", null);
		Specialty newSpecialty = new Specialty("Cardiology", null);
		Doctor doctor = new Doctor(user, oldSpecialty, "Old bio", 30, "Old clinic");

		doctor.updateAdminFields(newSpecialty, "New bio", 45, "New clinic");

		assertEquals(newSpecialty, doctor.getSpecialty());
		assertEquals("New bio", doctor.getBiography());
		assertEquals(45, doctor.getConsultationDurationMinutes());
		assertEquals("New clinic", doctor.getClinicAddress());
	}

	@Test
	void activateAndDeactivateSynchronizeDoctorAndUserStatuses() {
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Jane", "doctor@example.com", "hash", null);
		Doctor doctor = new Doctor(user, new Specialty("Cardiology", null), null, 30, null);

		doctor.deactivate();

		assertEquals("INACTIVE", doctor.getStatus());
		assertEquals(AccountStatus.INACTIVE, user.getAccountStatus());

		doctor.activate();

		assertEquals("ACTIVE", doctor.getStatus());
		assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());
	}
}
