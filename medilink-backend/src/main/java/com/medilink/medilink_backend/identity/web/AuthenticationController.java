package com.medilink.medilink_backend.identity.web;

import com.medilink.medilink_backend.identity.service.AuthenticationService;
import com.medilink.medilink_backend.shared.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	public AuthenticationController(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
		return ApiResponse.success(authenticationService.login(request));
	}

	@GetMapping("/me")
	public ApiResponse<AuthenticatedUserResponse> me(Authentication authentication) {
		return ApiResponse.success(authenticationService.currentUser(authentication.getName()));
	}
}
