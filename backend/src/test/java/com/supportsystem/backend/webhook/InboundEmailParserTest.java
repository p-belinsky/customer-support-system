package com.supportsystem.backend.webhook;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InboundEmailParserTest {

	@Test
	void extractTicketIdFindsDigitsAfterPlusInEnvelopeTo() {
		String envelope = "{\"to\":[\"support+42@inbound.example.com\"],\"from\":\"customer@example.com\"}";
		assertThat(InboundEmailParser.extractTicketId(envelope)).isEqualTo("42");
	}

	@Test
	void extractTicketIdReturnsNullForMalformedJson() {
		assertThat(InboundEmailParser.extractTicketId("not json")).isNull();
	}

	@Test
	void extractTicketIdReturnsNullWhenNoPlusAddressingPresent() {
		String envelope = "{\"to\":[\"support@inbound.example.com\"],\"from\":\"customer@example.com\"}";
		assertThat(InboundEmailParser.extractTicketId(envelope)).isNull();
	}

	@Test
	void extractTicketIdReturnsNullForBlankOrNullInput() {
		assertThat(InboundEmailParser.extractTicketId(null)).isNull();
		assertThat(InboundEmailParser.extractTicketId("")).isNull();
	}

	@Test
	void parseFromHeaderHandlesQuotedDisplayName() {
		InboundEmailParser.ParsedFrom parsed = InboundEmailParser.parseFromHeader("\"Jane Doe\" <jane@example.com>");
		assertThat(parsed.email()).isEqualTo("jane@example.com");
		assertThat(parsed.name()).isEqualTo("Jane Doe");
	}

	@Test
	void parseFromHeaderHandlesUnquotedDisplayName() {
		InboundEmailParser.ParsedFrom parsed = InboundEmailParser.parseFromHeader("Jane Doe <jane@example.com>");
		assertThat(parsed.email()).isEqualTo("jane@example.com");
		assertThat(parsed.name()).isEqualTo("Jane Doe");
	}

	@Test
	void parseFromHeaderHandlesBareAddress() {
		InboundEmailParser.ParsedFrom parsed = InboundEmailParser.parseFromHeader("jane@example.com");
		assertThat(parsed.email()).isEqualTo("jane@example.com");
		assertThat(parsed.name()).isNull();
	}

	@Test
	void extractMessageIdFindsHeaderCaseInsensitively() {
		String headers = "From: jane@example.com\nmessage-id: <abc123@mail.example.com>\nSubject: Hi\n";
		assertThat(InboundEmailParser.extractMessageId(headers)).isEqualTo("<abc123@mail.example.com>");
	}

	@Test
	void extractMessageIdFallsBackToRandomIdWhenHeaderAbsent() {
		String result = InboundEmailParser.extractMessageId("From: jane@example.com\nSubject: Hi\n");
		assertThat(result).startsWith("sendgrid-");
	}
}
