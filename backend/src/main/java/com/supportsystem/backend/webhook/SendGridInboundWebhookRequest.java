package com.supportsystem.backend.webhook;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendGridInboundWebhookRequest {

	private String from;
	private String subject;
	private String text;
	private String envelope;
	private String headers;
}
