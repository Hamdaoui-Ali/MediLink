package com.medilink.medilink_backend.shared.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityConfigTest {

	@Test
	void passwordEncoderHashesAndMatchesRawPassword() {
		PasswordEncoder passwordEncoder = new SecurityConfig().passwordEncoder();

		String encodedPassword = passwordEncoder.encode("Patient@123");

		assertNotEquals("Patient@123", encodedPassword);
		assertTrue(passwordEncoder.matches("Patient@123", encodedPassword));
	}

	@Test
	void jwtAuthenticationConverterMapsRoleClaimToSpringAuthority() {
		SecurityConfig securityConfig = new SecurityConfig();
		Jwt jwt = new Jwt(
				"jwt-token",
				Instant.now(),
				Instant.now().plusSeconds(3600),
				Map.of("alg", "HS256"),
				Map.of("sub", "admin@medilink.local", "role", "ADMIN")
		);

		var authentication = securityConfig.jwtAuthenticationConverter().convert(jwt);

		assertEquals("admin@medilink.local", authentication.getName());
		assertTrue(authentication.getAuthorities().stream()
				.anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
	}
}
