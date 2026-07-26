package com.supportsystem.backend.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WebhookAuthenticatorTest {

	@Test
	void matchingTokenIsAuthorized() {
		WebhookAuthenticator authenticator = new WebhookAuthenticator("secret123");
		assertThat(authenticator.isAuthorized("secret123")).isTrue();
	}

	@Test
	void wrongTokenIsUnauthorized() {
		WebhookAuthenticator authenticator = new WebhookAuthenticator("secret123");
		assertThat(authenticator.isAuthorized("wrong")).isFalse();
	}

	@Test
	void nullTokenIsUnauthorized() {
		WebhookAuthenticator authenticator = new WebhookAuthenticator("secret123");
		assertThat(authenticator.isAuthorized(null)).isFalse();
	}

	@Test
	void blankConfiguredSecretFailsClosed() {
		WebhookAuthenticator authenticator = new WebhookAuthenticator("");
		assertThat(authenticator.isAuthorized("anything")).isFalse();
	}
}
