package com.medilink.medilink_backend.availability.service;

import com.medilink.medilink_backend.appointment.domain.DoctorRef;
import com.medilink.medilink_backend.appointment.domain.Appointment;
import com.medilink.medilink_backend.appointment.domain.AppointmentStatus;
import com.medilink.medilink_backend.appointment.repository.AppointmentRepository;
import com.medilink.medilink_backend.appointment.repository.DoctorRefRepository;
import com.medilink.medilink_backend.appointment.service.DoctorRefNotFoundException;
import com.medilink.medilink_backend.availability.domain.DoctorAvailability;
import com.medilink.medilink_backend.availability.repository.DoctorAvailabilityRepository;
import com.medilink.medilink_backend.availability.web.AvailabilityRequest;
import com.medilink.medilink_backend.availability.web.AvailabilityResponse;
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
import java.util.Objects;

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

	public DoctorRef resolveDoctor(Long userId) {
		DoctorRef doctor = doctorRefRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorRefNotFoundException(userId));

		if (!doctor.isActive()) {
			throw new DoctorRefNotFoundException(userId);
		}

		return doctor;
	}

	@Transactional(readOnly = true)
	public List<AvailabilityResponse> listAvailability(Long doctorId) {
		return availabilityRepository.findByDoctorIdAndActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId)
				.stream()
				.map(this::toAvailabilityResponse)
				.toList();
	}

	@Transactional
	public AvailabilityResponse createAvailability(Long doctorId, AvailabilityRequest request) {
		validateRequest(doctorId, null, request);

		DoctorAvailability availability = new DoctorAvailability(
				doctorId,
				request.dayOfWeek(),
				request.startTime(),
				request.endTime()
		);

		return toAvailabilityResponse(availabilityRepository.save(availability));
	}

	@Transactional
	public AvailabilityResponse updateAvailability(Long doctorId, Long availabilityId, AvailabilityRequest request) {
		validateRequest(doctorId, availabilityId, request);

		DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
				.orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

		availability.update(request.dayOfWeek(), request.startTime(), request.endTime());
		return toAvailabilityResponse(availability);
	}

	@Transactional
	public void deleteAvailability(Long doctorId, Long availabilityId) {
		DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
				.orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

		availability.deactivate();
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

	private void validateRequest(Long doctorId, Long ignoredAvailabilityId, AvailabilityRequest request) {
		if (request.dayOfWeek() < 1 || request.dayOfWeek() > 7) {
			throw new InvalidAvailabilityException("Day of week must be between 1 and 7.");
		}

		if (!request.endTime().isAfter(request.startTime())) {
			throw new InvalidAvailabilityException("End time must be after start time.");
		}

		List<DoctorAvailability> sameDayAvailability = availabilityRepository
				.findByDoctorIdAndDayOfWeekAndActiveTrue(doctorId, request.dayOfWeek());

		boolean overlaps = sameDayAvailability.stream()
				.filter(availability -> ignoredAvailabilityId == null || !Objects.equals(availability.getId(), ignoredAvailabilityId))
				.anyMatch(availability -> request.startTime().isBefore(availability.getEndTime())
						&& request.endTime().isAfter(availability.getStartTime()));

		if (overlaps) {
			throw new InvalidAvailabilityException("Availability cannot overlap an existing active range.");
		}
	}

	private AvailabilityResponse toAvailabilityResponse(DoctorAvailability availability) {
		return new AvailabilityResponse(
				availability.getId(),
				availability.getDoctorId(),
				availability.getDayOfWeek(),
				availability.getStartTime(),
				availability.getEndTime()
		);
	}
}
