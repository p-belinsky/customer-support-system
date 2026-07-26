package com.supportsystem.backend.ticket.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import com.supportsystem.backend.ticket.Ticket;

public record TicketListResponse(
		List<TicketSummaryResponse> content,
		int page,
		int size,
		long totalElements,
		int totalPages) {

	public static TicketListResponse from(Page<Ticket> page) {
		return new TicketListResponse(
				page.getContent().stream().map(TicketSummaryResponse::from).toList(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}
}
