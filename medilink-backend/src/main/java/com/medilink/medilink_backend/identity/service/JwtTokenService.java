package com.medilink.medilink_backend.identity.service;

import com.medilink.medilink_backend.identity.domain.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtTokenService {

	private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(2);

	private final JwtEncoder jwtEncoder;

	public JwtTokenService(JwtEncoder jwtEncoder) {
		this.jwtEncoder = jwtEncoder;
	}

	public GeneratedToken generateAccessToken(User user) {
		Instant issuedAt = Instant.now();
		Instant expiresAt = issuedAt.plus(ACCESS_TOKEN_TTL);
		JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
				.issuer("medilink-backend")
				.issuedAt(issuedAt)
				.expiresAt(expiresAt)
				.subject(user.getEmail())
				.claim("email", user.getEmail())
				.claim("fullName", user.getFullName())
				.claim("role", user.getRole().getName().name());

		if (user.getId() != null) {
			claimsBuilder.claim("userId", user.getId());
		}

		JwtClaimsSet claims = claimsBuilder.build();

		JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
		return new GeneratedToken(token, "Bearer", expiresAt);
	}

	public record GeneratedToken(
			String accessToken,
			String tokenType,
			Instant expiresAt
	) {
	}
}
