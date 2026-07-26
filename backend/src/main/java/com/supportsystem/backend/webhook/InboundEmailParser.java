package com.supportsystem.backend.webhook;

import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
final class InboundEmailParser {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final Pattern MAILBOX_HASH_PATTERN = Pattern.compile("\\+(\\d+)@");
	private static final Pattern FROM_PATTERN = Pattern.compile("^\\s*\"?([^\"<]*)\"?\\s*<([^<>]+)>\\s*$");
	private static final Pattern MESSAGE_ID_HEADER_PATTERN =
			Pattern.compile("^Message-ID:\\s*(.+)$", Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);

	private InboundEmailParser() {
	}

	/** Preferred over the raw "to" field per SendGrid's guidance: envelope reflects true SMTP RCPT TO. */
	static String extractTicketId(String envelopeJson) {
		if (envelopeJson == null || envelopeJson.isBlank()) {
			return null;
		}
		try {
			Envelope envelope = OBJECT_MAPPER.readValue(envelopeJson, Envelope.class);
			if (envelope.to() == null) {
				return null;
			}
			for (String address : envelope.to()) {
				Matcher matcher = MAILBOX_HASH_PATTERN.matcher(address);
				if (matcher.find()) {
					return matcher.group(1);
				}
			}
		} catch (Exception e) {
			log.warn("Failed to parse SendGrid envelope field: {}", e.getMessage());
		}
		return null;
	}

	static ParsedFrom parseFromHeader(String rawFrom) {
		if (rawFrom == null || rawFrom.isBlank()) {
			return new ParsedFrom("", null);
		}
		Matcher matcher = FROM_PATTERN.matcher(rawFrom.trim());
		if (matcher.matches()) {
			String name = matcher.group(1).trim();
			return new ParsedFrom(matcher.group(2).trim(), name.isEmpty() ? null : name);
		}
		return new ParsedFrom(rawFrom.trim(), null);
	}

	static String extractMessageId(String rawHeaders) {
		if (rawHeaders != null) {
			Matcher matcher = MESSAGE_ID_HEADER_PATTERN.matcher(rawHeaders);
			if (matcher.find()) {
				return matcher.group(1).trim();
			}
		}
		return "sendgrid-" + UUID.randomUUID();
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	private record Envelope(List<String> to, String from) {
	}

	record ParsedFrom(String email, String name) {
	}
}
