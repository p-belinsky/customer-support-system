package com.supportsystem.backend.ticket.dto;

import java.util.List;

import com.supportsystem.backend.ticket.TicketStatus;

public record DashboardMetricsResponse(
		long totalTickets,
		Double resolutionRatePercent,
		long aiHandledCount,
		long humanHandledCount,
		List<StatusCount> byStatus,
		List<CategoryCount> byCategory) {

	public record StatusCount(TicketStatus status, long count) {
	}

	public record CategoryCount(String category, long count) {
	}
}
