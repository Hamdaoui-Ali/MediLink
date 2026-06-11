package com.medilink.medilink_backend.administration.web;

import com.medilink.medilink_backend.administration.service.SpecialtyService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/specialties")
public class SpecialtyController {

	private final SpecialtyService specialtyService;

	public SpecialtyController(SpecialtyService specialtyService) {
		this.specialtyService = specialtyService;
	}

	@GetMapping
	@PreAuthorize("#activeOnly or hasRole('ADMIN')")
	public ApiResponse<List<SpecialtyResponse>> list(
			@RequestParam(defaultValue = "true") boolean activeOnly
	) {
		return ApiResponse.success(specialtyService.list(activeOnly));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SpecialtyResponse> get(@PathVariable Long id) {
		return ApiResponse.success(specialtyService.get(id));
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<SpecialtyResponse>> create(@Valid @RequestBody SpecialtyRequest request) {
		return ResponseEntity
				.status(HttpStatus.CREATED)
				.body(ApiResponse.success(specialtyService.create(request)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SpecialtyResponse> update(
			@PathVariable Long id,
			@Valid @RequestBody SpecialtyRequest request
	) {
		return ApiResponse.success(specialtyService.update(id, request));
	}

	@PatchMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SpecialtyResponse> activate(@PathVariable Long id) {
		return ApiResponse.success(specialtyService.activate(id));
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SpecialtyResponse> deactivate(@PathVariable Long id) {
		return ApiResponse.success(specialtyService.deactivate(id));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<SpecialtyResponse> delete(@PathVariable Long id) {
		return ApiResponse.success(specialtyService.deactivate(id));
	}
}
