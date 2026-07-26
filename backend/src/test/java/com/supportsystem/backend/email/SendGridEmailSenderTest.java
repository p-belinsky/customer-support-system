package com.supportsystem.backend.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class SendGridEmailSenderTest {

	private static final String SEND_URL = "https://api.sendgrid.com/v3/mail/send";

	@Test
	void sendReturnsMessageIdFromResponseHeader() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		SendGridEmailSender sender = new SendGridEmailSender(builder, "test-api-key", "support@example.com");

		server.expect(requestTo(SEND_URL))
				.andExpect(method(HttpMethod.POST))
				.andExpect(header("Authorization", "Bearer test-api-key"))
				.andRespond(withStatus(HttpStatus.ACCEPTED)
						.header("X-Message-Id", "abc123")
						.contentType(MediaType.APPLICATION_JSON));

		EmailSendResult result = sender.send(new EmailMessage("customer@example.com", "Subject", "Body", "reply@example.com"));

		assertThat(result.providerMessageId()).isEqualTo("abc123");
		server.verify();
	}

	@Test
	void sendThrowsEmailSendExceptionOnFailureResponse() {
		RestClient.Builder builder = RestClient.builder();
		MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
		SendGridEmailSender sender = new SendGridEmailSender(builder, "test-api-key", "support@example.com");

		server.expect(requestTo(SEND_URL))
				.andRespond(withStatus(HttpStatus.BAD_REQUEST)
						.contentType(MediaType.APPLICATION_JSON)
						.body("{\"errors\":[{\"message\":\"sender not verified\"}]}"));

		assertThatThrownBy(() -> sender.send(new EmailMessage("customer@example.com", "Subject", "Body", "reply@example.com")))
				.isInstanceOf(EmailSendException.class)
				.hasMessageContaining("400")
				.hasMessageContaining("sender not verified");
	}
}
