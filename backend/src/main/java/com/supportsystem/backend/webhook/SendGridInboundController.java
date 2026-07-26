package com.supportsystem.backend.webhook;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supportsystem.backend.ticket.TicketService;

@RestController
@RequestMapping("/api/webhooks/sendgrid")
public class SendGridInboundController {

	private final TicketService ticketService;
	private final WebhookAuthenticator webhookAuthenticator;

	public SendGridInboundController(TicketService ticketService, WebhookAuthenticator webhookAuthenticator) {
		this.ticketService = ticketService;
		this.webhookAuthenticator = webhookAuthenticator;
	}

	@PostMapping(value = "/inbound", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Void> inbound(
			@RequestParam(required = false) String token,
			@ModelAttribute SendGridInboundWebhookRequest request) {
		if (!webhookAuthenticator.isAuthorized(token)) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}

		InboundEmailParser.ParsedFrom parsedFrom = InboundEmailParser.parseFromHeader(request.getFrom());
		String mailboxHash = InboundEmailParser.extractTicketId(request.getEnvelope());
		String messageId = InboundEmailParser.extractMessageId(request.getHeaders());
		String subject = (request.getSubject() == null || request.getSubject().isBlank())
				? "(no subject)" : request.getSubject();
		String body = request.getText() == null ? "" : request.getText();

		ticketService.processInboundEmail(
				parsedFrom.email(), parsedFrom.name(), subject, mailboxHash, body, messageId);

		return ResponseEntity.ok().build();
	}
}
