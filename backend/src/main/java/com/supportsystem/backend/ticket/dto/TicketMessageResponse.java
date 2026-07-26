package com.supportsystem.backend.ticket.dto;

import java.time.Instant;

import com.supportsystem.backend.ticket.MessageDirection;
import com.supportsystem.backend.ticket.TicketMessage;

public record TicketMessageResponse(
		Long id,
		MessageDirection direction,
		String senderEmail,
		String body,
		Instant createdAt) {

	public static TicketMessageResponse from(TicketMessage message) {
		return new TicketMessageResponse(
				message.getId(),
				message.getDirection(),
				message.getSenderEmail(),
				message.getBody(),
				message.getCreatedAt());
	}
}
