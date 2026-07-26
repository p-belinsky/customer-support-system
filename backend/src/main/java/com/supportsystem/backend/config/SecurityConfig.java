package com.supportsystem.backend.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tools.jackson.databind.ObjectMapper;

@Configuration
public class SecurityConfig {

	@Value("${cors.allowed-origins}")
	private String allowedOrigins;

	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
		configuration.setAllowedMethods(List.of("GET", "POST", "PUT","PATCH","DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(List.of("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		ObjectMapper mapper = new ObjectMapper();

		http
			.csrf(csrf -> csrf.disable())
			.cors(cors -> {})
			.authorizeHttpRequests(auth -> auth
					.requestMatchers("/api/health", "/api/auth/login", "/api/webhooks/sendgrid/inbound").permitAll()
					.anyRequest().authenticated())
			.formLogin(form -> form
					.loginProcessingUrl("/api/auth/login")
					.successHandler((request, response, authentication) -> {
						response.setStatus(HttpStatus.OK.value());
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						mapper.writeValue(response.getWriter(), Map.of("username", authentication.getName()));
					})
					.failureHandler((request, response, exception) -> {
						response.setStatus(HttpStatus.UNAUTHORIZED.value());
						response.setContentType(MediaType.APPLICATION_JSON_VALUE);
						mapper.writeValue(response.getWriter(), Map.of("error", "Invalid username or password"));
					}))
			.logout(logout -> logout
					.logoutRequestMatcher(PathPatternRequestMatcher.pathPattern(HttpMethod.POST, "/api/auth/logout"))
					.logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.OK.value())))
			.exceptionHandling(exceptions -> exceptions
					.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));

		return http.build();
	}
}
