package com.supportsystem.backend.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthControllerTest {

	@Test
	void meReturnsUsernameFromAuthentication() {
		AuthController controller = new AuthController();
		Authentication authentication = new UsernamePasswordAuthenticationToken("admin", null);

		Map<String, Object> result = controller.me(authentication);

		assertThat(result).containsEntry("username", "admin");
	}
}
