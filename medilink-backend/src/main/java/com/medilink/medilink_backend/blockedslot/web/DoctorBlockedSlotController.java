package com.medilink.medilink_backend.blockedslot.web;

import com.medilink.medilink_backend.blockedslot.service.BlockedSlotService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/doctor/blocked-slots")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorBlockedSlotController {

	private final BlockedSlotService blockedSlotService;

	public DoctorBlockedSlotController(BlockedSlotService blockedSlotService) {
		this.blockedSlotService = blockedSlotService;
	}

	@GetMapping
	public ApiResponse<List<BlockedSlotResponse>> listBlockedSlots(JwtAuthenticationToken jwt) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(blockedSlotService.listBlockedSlots(doctorId));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<BlockedSlotResponse> createBlockedSlot(
			JwtAuthenticationToken jwt,
			@Valid @RequestBody BlockedSlotRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(blockedSlotService.createBlockedSlot(doctorId, request));
	}

	@PatchMapping("/{id}")
	public ApiResponse<BlockedSlotResponse> updateBlockedSlot(
			JwtAuthenticationToken jwt,
			@PathVariable Long id,
			@Valid @RequestBody BlockedSlotRequest request
	) {
		Long doctorId = resolveDoctorId(jwt);
		return ApiResponse.success(blockedSlotService.updateBlockedSlot(doctorId, id, request));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ApiResponse<Void> deleteBlockedSlot(JwtAuthenticationToken jwt, @PathVariable Long id) {
		Long doctorId = resolveDoctorId(jwt);
		blockedSlotService.deleteBlockedSlot(doctorId, id);
		return ApiResponse.success(null);
	}

	private Long resolveDoctorId(JwtAuthenticationToken jwt) {
		Long userId = jwt.getToken().getClaim("userId");
		return blockedSlotService.resolveDoctor(userId).getId();
	}
}
