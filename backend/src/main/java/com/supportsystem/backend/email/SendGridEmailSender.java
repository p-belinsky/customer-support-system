package com.supportsystem.backend.email;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class SendGridEmailSender implements EmailSender {

	private static final String SENDGRID_SEND_URL = "https://api.sendgrid.com/v3/mail/send";

	private final RestClient restClient;
	private final String fromAddress;

	public SendGridEmailSender(RestClient.Builder restClientBuilder,
			@Value("${sendgrid.api-key}") String apiKey,
			@Value("${sendgrid.from-address}") String fromAddress) {
		this.restClient = restClientBuilder
				.baseUrl(SENDGRID_SEND_URL)
				.defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
				.defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
				.build();
		this.fromAddress = fromAddress;
	}

	@Override
	public EmailSendResult send(EmailMessage message) {
		Map<String, Object> body = Map.of(
				"personalizations", List.of(Map.of(
						"to", List.of(Map.of("email", message.to())))),
				"from", Map.of("email", fromAddress),
				"reply_to", Map.of("email", message.replyTo()),
				"subject", message.subject(),
				"content", List.of(Map.of("type", "text/plain", "value", message.textBody())));

		try {
			ResponseEntity<Void> response = restClient.post()
					.contentType(MediaType.APPLICATION_JSON)
					.body(body)
					.retrieve()
					.toBodilessEntity();

			String messageId = response.getHeaders().getFirst("X-Message-Id");
			return new EmailSendResult(messageId);
		} catch (RestClientResponseException e) {
			throw new EmailSendException(
					"SendGrid send failed with status " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
		}
	}
}
