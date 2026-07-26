package com.supportsystem.backend.ticket.dto;

import java.time.Instant;
import java.util.List;

import com.supportsystem.backend.ticket.Ticket;
import com.supportsystem.backend.ticket.TicketMessage;
import com.supportsystem.backend.ticket.TicketStatus;

public record TicketDetailResponse(
		Long id,
		String customerEmail,
		String customerName,
		String subject,
		TicketStatus status,
		Instant createdAt,
		Instant updatedAt,
		List<TicketMessageResponse> messages) {

	public static TicketDetailResponse from(Ticket ticket, List<TicketMessage> messages) {
		return new TicketDetailResponse(
				ticket.getId(),
				ticket.getCustomerEmail(),
				ticket.getCustomerName(),
				ticket.getSubject(),
				ticket.getStatus(),
				ticket.getCreatedAt(),
				ticket.getUpdatedAt(),
				messages.stream().map(TicketMessageResponse::from).toList());
	}
}
