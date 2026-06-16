package com.medilink.medilink_backend.availability.service;

import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.availability.domain.DoctorAvailability;
import com.medilink.medilink_backend.availability.repository.DoctorAvailabilityRepository;
import com.medilink.medilink_backend.availability.web.SlotResponse;
import com.medilink.medilink_backend.blockedslot.domain.BlockedSlot;
import com.medilink.medilink_backend.blockedslot.repository.BlockedSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorAvailabilityService {

	private final DoctorAvailabilityRepository availabilityRepository;
	private final BlockedSlotRepository blockedSlotRepository;
	private final AppointmentRepository appointmentRepository;
	private final DoctorRefRepository doctorRefRepository;

	public DoctorAvailabilityService(
			DoctorAvailabilityRepository availabilityRepository,
			BlockedSlotRepository blockedSlotRepository,
			AppointmentRepository appointmentRepository,
			DoctorRefRepository doctorRefRepository
	) {
		this.availabilityRepository = availabilityRepository;
		this.blockedSlotRepository = blockedSlotRepository;
		this.appointmentRepository = appointmentRepository;
		this.doctorRefRepository = doctorRefRepository;
	}

	@Transactional(readOnly = true)
	public List<SlotResponse> getAvailableSlots(Long doctorId, LocalDate date) {
		int dayOfWeek = date.getDayOfWeek().getValue();

		List<DoctorAvailability> availabilities = availabilityRepository
				.findByDoctorIdAndDayOfWeekAndActiveTrue(doctorId, dayOfWeek);

		if (availabilities.isEmpty()) {
			return List.of();
		}

		int durationMinutes = doctorRefRepository.findConsultationDurationById(doctorId).orElse(30);

		List<BlockedSlot> blockedSlots = blockedSlotRepository
				.findByDoctorIdAndBlockDateAndActiveTrue(doctorId, date);

		List<AppointmentStatus> activeStatuses = List.of(
				AppointmentStatus.CONFIRMED, AppointmentStatus.RESCHEDULED);
		List<Appointment> existingAppointments = appointmentRepository
				.findByDoctorIdAndAppointmentDateAndStatusIn(doctorId, date, activeStatuses);

		List<SlotResponse> availableSlots = new ArrayList<>();
		LocalTime now = LocalTime.now();
		boolean isToday = date.equals(LocalDate.now());

		for (DoctorAvailability availability : availabilities) {
			LocalTime slotStart = availability.getStartTime();
			LocalTime windowEnd = availability.getEndTime();

			while (slotStart.plusMinutes(durationMinutes).compareTo(windowEnd) <= 0) {
				LocalTime slotEnd = slotStart.plusMinutes(durationMinutes);

				if (isToday && slotStart.isBefore(now)) {
					slotStart = slotStart.plusMinutes(durationMinutes);
					continue;
				}

				boolean blocked = isSlotBlockedByBlockedSlot(slotStart, slotEnd, blockedSlots);
				boolean booked = isSlotBooked(slotStart, slotEnd, existingAppointments);

				if (!blocked && !booked) {
					availableSlots.add(new SlotResponse(slotStart, slotEnd));
				}

				slotStart = slotStart.plusMinutes(durationMinutes);
			}
		}

		return availableSlots;
	}

	private boolean isSlotBlockedByBlockedSlot(LocalTime start, LocalTime end, List<BlockedSlot> blockedSlots) {
		for (BlockedSlot blocked : blockedSlots) {
			if (start.isBefore(blocked.getEndTime()) && end.isAfter(blocked.getStartTime())) {
				return true;
			}
		}
		return false;
	}

	private boolean isSlotBooked(LocalTime start, LocalTime end, List<Appointment> appointments) {
		for (Appointment appointment : appointments) {
			if (start.isBefore(appointment.getEndTime()) && end.isAfter(appointment.getStartTime())) {
				return true;
			}
		}
		return false;
	}
}
