package com.medilink.medilink_backend.doctor.service;

import com.medilink.medilink_backend.doctor.domain.Doctor;
import com.medilink.medilink_backend.doctor.domain.DoctorAvailability;
import com.medilink.medilink_backend.doctor.domain.DoctorStatus;
import com.medilink.medilink_backend.doctor.repository.DoctorAvailabilityRepository;
import com.medilink.medilink_backend.doctor.repository.DoctorRepository;
import com.medilink.medilink_backend.doctor.web.DoctorAvailabilityRequest;
import com.medilink.medilink_backend.doctor.web.DoctorAvailabilityResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class DoctorAvailabilityService {

	private final DoctorAvailabilityRepository availabilityRepository;
	private final DoctorRepository doctorRepository;

	public DoctorAvailabilityService(
			DoctorAvailabilityRepository availabilityRepository,
			DoctorRepository doctorRepository
	) {
		this.availabilityRepository = availabilityRepository;
		this.doctorRepository = doctorRepository;
	}

	@Transactional(readOnly = true)
	public Doctor resolveDoctor(Long userId) {
		Doctor doctor = doctorRepository.findByUserId(userId)
				.orElseThrow(() -> new DoctorNotFoundException(userId));

		if (doctor.getStatus() != DoctorStatus.ACTIVE) {
			throw new DoctorNotFoundException(userId);
		}

		return doctor;
	}

	@Transactional(readOnly = true)
	public List<DoctorAvailabilityResponse> listAvailability(Long doctorId) {
		return availabilityRepository.findByDoctorIdAndIsActiveTrueOrderByDayOfWeekAscStartTimeAsc(doctorId)
				.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional
	public DoctorAvailabilityResponse addAvailability(Long doctorId, DoctorAvailabilityRequest request) {
		LocalTime start = parseTime(request.startTime());
		LocalTime end = parseTime(request.endTime());

		if (!end.isAfter(start)) {
			throw new InvalidAvailabilityException("End time must be after start time");
		}

		DoctorAvailability availability = new DoctorAvailability(doctorId, request.dayOfWeek(), start, end);
		DoctorAvailability saved = availabilityRepository.save(availability);
		return toResponse(saved);
	}

	@Transactional
	public DoctorAvailabilityResponse updateAvailability(Long doctorId, Long availabilityId, DoctorAvailabilityRequest request) {
		DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
				.orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

		LocalTime start = parseTime(request.startTime());
		LocalTime end = parseTime(request.endTime());

		if (!end.isAfter(start)) {
			throw new InvalidAvailabilityException("End time must be after start time");
		}

		availability.update(request.dayOfWeek(), start, end);
		return toResponse(availability);
	}

	@Transactional
	public DoctorAvailabilityResponse deactivateAvailability(Long doctorId, Long availabilityId) {
		DoctorAvailability availability = availabilityRepository.findByIdAndDoctorId(availabilityId, doctorId)
				.orElseThrow(() -> new AvailabilityNotFoundException(availabilityId));

		availability.deactivate();
		return toResponse(availability);
	}

	private LocalTime parseTime(String time) {
		try {
			return LocalTime.parse(time);
		} catch (DateTimeParseException e) {
			throw new InvalidAvailabilityException("Invalid time format. Use HH:mm or HH:mm:ss");
		}
	}

	private DoctorAvailabilityResponse toResponse(DoctorAvailability availability) {
		return new DoctorAvailabilityResponse(
				availability.getId(),
				availability.getDayOfWeek(),
				availability.getStartTime(),
				availability.getEndTime(),
				availability.isActive()
		);
	}
}
