package com.supportsystem.backend.ticket.dto;

import java.time.Instant;

import com.supportsystem.backend.ticket.Ticket;
import com.supportsystem.backend.ticket.TicketStatus;

public record TicketSummaryResponse(
		Long id,
		String customerEmail,
		String customerName,
		String subject,
		String category,
		TicketStatus status,
		Instant createdAt,
		Instant lastMessageAt) {

	public static TicketSummaryResponse from(Ticket ticket) {
		return new TicketSummaryResponse(
				ticket.getId(),
				ticket.getCustomerEmail(),
				ticket.getCustomerName(),
				ticket.getSubject(),
				ticket.getCategory(),
				ticket.getStatus(),
				ticket.getCreatedAt(),
				ticket.getLastMessageAt());
	}
}
