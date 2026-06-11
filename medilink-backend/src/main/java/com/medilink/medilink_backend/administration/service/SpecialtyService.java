package com.medilink.medilink_backend.administration.service;

import com.medilink.medilink_backend.administration.domain.Specialty;
import com.medilink.medilink_backend.administration.domain.SpecialtyStatus;
import com.medilink.medilink_backend.administration.repository.SpecialtyRepository;
import com.medilink.medilink_backend.administration.web.SpecialtyRequest;
import com.medilink.medilink_backend.administration.web.SpecialtyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SpecialtyService {

	private final SpecialtyRepository specialtyRepository;

	public SpecialtyService(SpecialtyRepository specialtyRepository) {
		this.specialtyRepository = specialtyRepository;
	}

	@Transactional(readOnly = true)
	public List<SpecialtyResponse> list(boolean activeOnly) {
		List<Specialty> specialties = activeOnly
				? specialtyRepository.findAllByStatusOrderByNameAsc(SpecialtyStatus.ACTIVE)
				: specialtyRepository.findAllByOrderByNameAsc();

		return specialties.stream()
				.map(this::toResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public SpecialtyResponse get(Long id) {
		return toResponse(findById(id));
	}

	@Transactional(readOnly = true)
	public Specialty getActiveSpecialty(Long id) {
		return specialtyRepository.findByIdAndStatus(id, SpecialtyStatus.ACTIVE)
				.orElseThrow(() -> new SpecialtyNotFoundException(id));
	}

	@Transactional
	public SpecialtyResponse create(SpecialtyRequest request) {
		String name = normalizeName(request.name());

		if (specialtyRepository.existsByNameIgnoreCase(name)) {
			throw new DuplicateSpecialtyNameException(name);
		}

		Specialty specialty = specialtyRepository.save(new Specialty(name, trimToNull(request.description())));
		return toResponse(specialty);
	}

	@Transactional
	public SpecialtyResponse update(Long id, SpecialtyRequest request) {
		Specialty specialty = findById(id);
		String name = normalizeName(request.name());

		if (specialtyRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
			throw new DuplicateSpecialtyNameException(name);
		}

		specialty.update(name, trimToNull(request.description()));
		return toResponse(specialty);
	}

	@Transactional
	public SpecialtyResponse activate(Long id) {
		Specialty specialty = findById(id);
		specialty.activate();
		return toResponse(specialty);
	}

	@Transactional
	public SpecialtyResponse deactivate(Long id) {
		Specialty specialty = findById(id);
		specialty.deactivate();
		return toResponse(specialty);
	}

	String normalizeName(String name) {
		return name.trim();
	}

	String trimToNull(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}

		return value.trim();
	}

	private Specialty findById(Long id) {
		return specialtyRepository.findById(id)
				.orElseThrow(() -> new SpecialtyNotFoundException(id));
	}

	private SpecialtyResponse toResponse(Specialty specialty) {
		return new SpecialtyResponse(
				specialty.getId(),
				specialty.getName(),
				specialty.getDescription(),
				specialty.getStatus()
		);
	}
}
