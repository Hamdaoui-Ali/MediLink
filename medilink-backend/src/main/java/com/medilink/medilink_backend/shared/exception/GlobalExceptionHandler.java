package com.medilink.medilink_backend.shared.exception;

import com.medilink.medilink_backend.administration.service.DuplicateSpecialtyNameException;
import com.medilink.medilink_backend.administration.service.SpecialtyNotFoundException;
import com.medilink.medilink_backend.appointment.service.AppointmentNotFoundException;
import com.medilink.medilink_backend.appointment.service.DoctorRefNotFoundException;
import com.medilink.medilink_backend.appointment.service.InvalidAppointmentStatusException;
import com.medilink.medilink_backend.appointment.service.PatientNotFoundException;
import com.medilink.medilink_backend.appointment.service.SlotUnavailableException;
import com.medilink.medilink_backend.availability.service.AvailabilityNotFoundException;
import com.medilink.medilink_backend.availability.service.InvalidAvailabilityException;
import com.medilink.medilink_backend.blockedslot.service.BlockedSlotNotFoundException;
import com.medilink.medilink_backend.blockedslot.service.InvalidBlockedSlotException;
import com.medilink.medilink_backend.doctor.service.DoctorNotFoundException;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.InactiveAccountException;
import com.medilink.medilink_backend.identity.service.InvalidCredentialsException;
import com.medilink.medilink_backend.identity.service.RoleNotFoundException;
import com.medilink.medilink_backend.identity.service.UserNotFoundException;
import com.medilink.medilink_backend.patient.service.DuplicateEmailException;
import com.medilink.medilink_backend.shared.api.ApiError;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException exception) {
		Map<String, String> details = new LinkedHashMap<>();
		exception.getBindingResult().getFieldErrors()
				.forEach(error -> details.put(error.getField(), error.getDefaultMessage()));

		return ResponseEntity.badRequest().body(ApiResponse.failure(
				new ApiError("VALIDATION_ERROR", "Request validation failed", details)
		));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	ResponseEntity<ApiResponse<Void>> handleConstraintViolation(ConstraintViolationException exception) {
		Map<String, String> details = new LinkedHashMap<>();
		exception.getConstraintViolations()
				.forEach(violation -> details.put(violation.getPropertyPath().toString(), violation.getMessage()));

		return ResponseEntity.badRequest().body(ApiResponse.failure(
				new ApiError("VALIDATION_ERROR", "Constraint validation failed", details)
		));
	}

	@ExceptionHandler(DuplicateEmailException.class)
	ResponseEntity<ApiResponse<Void>> handleDuplicateEmail(DuplicateEmailException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(
				new ApiError("DUPLICATE_EMAIL", "An account with this email already exists", Map.of())
		));
	}

	@ExceptionHandler(DuplicateSpecialtyNameException.class)
	ResponseEntity<ApiResponse<Void>> handleDuplicateSpecialtyName(DuplicateSpecialtyNameException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(
				new ApiError("DUPLICATE_SPECIALTY_NAME", "A specialty with this name already exists", Map.of())
		));
	}

	@ExceptionHandler(SpecialtyNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleSpecialtyNotFound(SpecialtyNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("SPECIALTY_NOT_FOUND", "Specialty was not found", Map.of())
		));
	}

	@ExceptionHandler(EmailAlreadyUsedException.class)
	ResponseEntity<ApiResponse<Void>> handleEmailAlreadyUsed(EmailAlreadyUsedException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(
				new ApiError("DUPLICATE_EMAIL", "An account with this email already exists", Map.of())
		));
	}

	@ExceptionHandler(UserNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleUserNotFound(UserNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("USER_NOT_FOUND", "User was not found", Map.of())
		));
	}

	@ExceptionHandler(RoleNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleRoleNotFound(RoleNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(
				new ApiError("ROLE_NOT_FOUND", "Required role data is missing", Map.of())
		));
	}

	@ExceptionHandler(InvalidCredentialsException.class)
	ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException exception) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.failure(
				new ApiError("INVALID_CREDENTIALS", "Invalid email or password", Map.of())
		));
	}

	@ExceptionHandler(InactiveAccountException.class)
	ResponseEntity<ApiResponse<Void>> handleInactiveAccount(InactiveAccountException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(
				new ApiError("INACTIVE_ACCOUNT", "Account is not active", Map.of())
		));
	}

	@ExceptionHandler(AppointmentNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleAppointmentNotFound(AppointmentNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("APPOINTMENT_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(DoctorRefNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleDoctorRefNotFound(DoctorRefNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("DOCTOR_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(InvalidAppointmentStatusException.class)
	ResponseEntity<ApiResponse<Void>> handleInvalidAppointmentStatus(InvalidAppointmentStatusException exception) {
		return ResponseEntity.badRequest().body(ApiResponse.failure(
				new ApiError("INVALID_STATUS_TRANSITION", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(PatientNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handlePatientNotFound(PatientNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("PATIENT_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(com.medilink.medilink_backend.patient.service.PatientNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleManagedPatientNotFound(
			com.medilink.medilink_backend.patient.service.PatientNotFoundException exception
	) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("PATIENT_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(SlotUnavailableException.class)
	ResponseEntity<ApiResponse<Void>> handleSlotUnavailable(SlotUnavailableException exception) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(
				new ApiError("SLOT_UNAVAILABLE", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(BlockedSlotNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleBlockedSlotNotFound(BlockedSlotNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("BLOCKED_SLOT_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(InvalidBlockedSlotException.class)
	ResponseEntity<ApiResponse<Void>> handleInvalidBlockedSlot(InvalidBlockedSlotException exception) {
		return ResponseEntity.badRequest().body(ApiResponse.failure(
				new ApiError("INVALID_BLOCKED_SLOT", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(AvailabilityNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleAvailabilityNotFound(AvailabilityNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("AVAILABILITY_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(InvalidAvailabilityException.class)
	ResponseEntity<ApiResponse<Void>> handleInvalidAvailability(InvalidAvailabilityException exception) {
		return ResponseEntity.badRequest().body(ApiResponse.failure(
				new ApiError("INVALID_AVAILABILITY", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(DoctorNotFoundException.class)
	ResponseEntity<ApiResponse<Void>> handleDoctorNotFound(DoctorNotFoundException exception) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.failure(
				new ApiError("DOCTOR_NOT_FOUND", exception.getMessage(), Map.of())
		));
	}

	@ExceptionHandler(AccessDeniedException.class)
	ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException exception) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.failure(
				new ApiError("ACCESS_DENIED", "You do not have permission to perform this action", Map.of())
		));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiResponse<Void>> handleUnexpected(Exception exception, HttpServletRequest request) {
		Map<String, String> details = Map.of("path", request.getRequestURI());
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(
				new ApiError("INTERNAL_SERVER_ERROR", exception.getMessage(), details)
		));
	}
}
