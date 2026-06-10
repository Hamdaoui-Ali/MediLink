package com.medilink.medilink_backend.shared.exception;

import com.medilink.medilink_backend.administration.service.DuplicateSpecialtyNameException;
import com.medilink.medilink_backend.administration.service.SpecialtyNotFoundException;
import com.medilink.medilink_backend.identity.service.EmailAlreadyUsedException;
import com.medilink.medilink_backend.identity.service.InactiveAccountException;
import com.medilink.medilink_backend.identity.service.InvalidCredentialsException;
import com.medilink.medilink_backend.identity.service.RoleNotFoundException;
import com.medilink.medilink_backend.identity.service.UserNotFoundException;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.patient.service.DuplicateEmailException;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

	@Test
	void handleDuplicateEmailReturnsConflictResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateEmail(
				new DuplicateEmailException("jane@example.com")
		);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertNotNull(response.getBody().error());
		assertEquals("DUPLICATE_EMAIL", response.getBody().error().code());
	}

	@Test
	void handleDuplicateSpecialtyNameReturnsConflictResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleDuplicateSpecialtyName(
				new DuplicateSpecialtyNameException("Cardiology")
		);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("DUPLICATE_SPECIALTY_NAME", response.getBody().error().code());
	}

	@Test
	void handleSpecialtyNotFoundReturnsNotFoundResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleSpecialtyNotFound(new SpecialtyNotFoundException(1L));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("SPECIALTY_NOT_FOUND", response.getBody().error().code());
	}

	@Test
	void handleEmailAlreadyUsedReturnsConflictResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleEmailAlreadyUsed(
				new EmailAlreadyUsedException("jane@example.com")
		);

		assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("DUPLICATE_EMAIL", response.getBody().error().code());
	}

	@Test
	void handleUserNotFoundReturnsNotFoundResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleUserNotFound(new UserNotFoundException(1L));

		assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("USER_NOT_FOUND", response.getBody().error().code());
	}

	@Test
	void handleRoleNotFoundReturnsServerErrorResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleRoleNotFound(new RoleNotFoundException(RoleName.PATIENT));

		assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("ROLE_NOT_FOUND", response.getBody().error().code());
	}

	@Test
	void handleInvalidCredentialsReturnsUnauthorizedResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleInvalidCredentials(new InvalidCredentialsException());

		assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("INVALID_CREDENTIALS", response.getBody().error().code());
	}

	@Test
	void handleInactiveAccountReturnsForbiddenResponse() {
		GlobalExceptionHandler handler = new GlobalExceptionHandler();

		ResponseEntity<ApiResponse<Void>> response = handler.handleInactiveAccount(new InactiveAccountException());

		assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
		assertNotNull(response.getBody());
		assertFalse(response.getBody().success());
		assertEquals("INACTIVE_ACCOUNT", response.getBody().error().code());
	}
}
