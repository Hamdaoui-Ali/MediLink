package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.Role;
import com.medilink.medilink_backend.identity.domain.RoleName;
import com.medilink.medilink_backend.identity.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtTokenServiceTest {

	@Test
	void generateAccessTokenCreatesRoleAndIdentityClaims() {
		JwtEncoder jwtEncoder = mock(JwtEncoder.class);
		when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
			JwtEncoderParameters parameters = invocation.getArgument(0);
			return new Jwt(
					"jwt-token",
					parameters.getClaims().getIssuedAt(),
					parameters.getClaims().getExpiresAt(),
					Map.of("alg", "HS256"),
					parameters.getClaims().getClaims()
			);
		});
		JwtTokenService tokenService = new JwtTokenService(jwtEncoder);
		User user = new User(new Role(RoleName.DOCTOR, "Doctor"), "Dr Care", "doctor@example.com", "hash", null);

		JwtTokenService.GeneratedToken token = tokenService.generateAccessToken(user);

		assertEquals("jwt-token", token.accessToken());
		assertEquals("Bearer", token.tokenType());
		assertTrue(token.expiresAt().isAfter(Instant.now()));
	}
}
