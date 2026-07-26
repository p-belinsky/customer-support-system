package com.supportsystem.backend.webhook;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookAuthenticator {

	private final String expectedToken;

	public WebhookAuthenticator(@Value("${sendgrid.webhook.secret}") String expectedToken) {
		this.expectedToken = expectedToken;
	}

	public boolean isAuthorized(String token) {
		if (token == null || expectedToken == null || expectedToken.isBlank()) {
			return false;
		}
		return constantTimeEquals(token, expectedToken);
	}

	private boolean constantTimeEquals(String a, String b) {
		return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
	}
}
