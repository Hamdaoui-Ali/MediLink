package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.AccountStatus;
import com.medilink.medilink_backend.identity.domain.User;
import com.medilink.medilink_backend.identity.repository.UserRepository;
import com.medilink.medilink_backend.identity.web.AuthenticatedUserResponse;
import com.medilink.medilink_backend.identity.web.LoginRequest;
import com.medilink.medilink_backend.identity.web.LoginResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticationService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtTokenService jwtTokenService;

	public AuthenticationService(
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			JwtTokenService jwtTokenService
	) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtTokenService = jwtTokenService;
	}

	@Transactional(readOnly = true)
	public LoginResponse login(LoginRequest request) {
		User user = userRepository.findByEmailIgnoreCase(normalizeEmail(request.email()))
				.orElseThrow(InvalidCredentialsException::new);

		if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
			throw new InvalidCredentialsException();
		}

		if (user.getAccountStatus() != AccountStatus.ACTIVE) {
			throw new InactiveAccountException();
		}

		JwtTokenService.GeneratedToken token = jwtTokenService.generateAccessToken(user);
		return new LoginResponse(
				token.accessToken(),
				token.tokenType(),
				token.expiresAt(),
				toAuthenticatedUser(user)
		);
	}

	@Transactional(readOnly = true)
	public AuthenticatedUserResponse currentUser(String email) {
		User user = userRepository.findByEmailIgnoreCase(normalizeEmail(email))
				.orElseThrow(() -> new UserNotFoundException(email));

		return toAuthenticatedUser(user);
	}

	String normalizeEmail(String email) {
		return email.trim().toLowerCase();
	}

	private AuthenticatedUserResponse toAuthenticatedUser(User user) {
		return new AuthenticatedUserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				user.getRole().getName(),
				user.getAccountStatus()
		);
	}
}
