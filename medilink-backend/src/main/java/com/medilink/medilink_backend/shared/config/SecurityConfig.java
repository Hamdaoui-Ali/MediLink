package com.medilink.medilink_backend.shared.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(authorize -> authorize
						.requestMatchers(
								"/v1/health",
								"/api/v1/health",
								"/v1/auth/login",
								"/api/v1/auth/login",
								"/v1/patients/register",
								"/api/v1/patients/register",
								"/actuator/health",
								"/error"
						).permitAll()
						.requestMatchers(HttpMethod.GET, "/v1/specialties", "/v1/specialties/*").permitAll()
						.anyRequest().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
				.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	JwtEncoder jwtEncoder(@Value("${medilink.security.jwt.secret}") String secret) {
		return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey(secret)));
	}

	@Bean
	JwtDecoder jwtDecoder(@Value("${medilink.security.jwt.secret}") String secret) {
		return NimbusJwtDecoder.withSecretKey(jwtSecretKey(secret)).build();
	}

	Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
		return jwt -> {
			String role = jwt.getClaimAsString("role");
			List<SimpleGrantedAuthority> authorities = role == null
					? List.of()
					: List.of(new SimpleGrantedAuthority("ROLE_" + role));

			return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
		};
	}

	private SecretKey jwtSecretKey(String secret) {
		return new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
	}
}
