package com.supportsystem.backend.ticket;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.supportsystem.backend.ticket.TicketRepository.CategoryCountRow;
import com.supportsystem.backend.ticket.TicketRepository.StatusCountRow;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse.CategoryCount;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse.StatusCount;

@Service
public class TicketMetricsService {

	private static final List<TicketStatus> STATUS_ORDER = List.of(
			TicketStatus.NEW,
			TicketStatus.AI_RESPONDED,
			TicketStatus.NEEDS_REVIEW,
			TicketStatus.ESCALATED,
			TicketStatus.PENDING_CUSTOMER,
			TicketStatus.RESOLVED,
			TicketStatus.CLOSED);

	private final TicketRepository ticketRepository;

	public TicketMetricsService(TicketRepository ticketRepository) {
		this.ticketRepository = ticketRepository;
	}

	public DashboardMetricsResponse computeMetrics() {
		EnumMap<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
		for (TicketStatus status : TicketStatus.values()) {
			counts.put(status, 0L);
		}
		for (StatusCountRow row : ticketRepository.countByStatusGrouped()) {
			counts.put(row.getStatus(), row.getCount());
		}

		long totalTickets = counts.values().stream().mapToLong(Long::longValue).sum();
		long resolvedOrClosed = counts.get(TicketStatus.RESOLVED) + counts.get(TicketStatus.CLOSED);
		Double resolutionRatePercent = totalTickets == 0 ? null : 100.0 * resolvedOrClosed / totalTickets;

		long aiHandledCount = counts.get(TicketStatus.AI_RESPONDED);
		long humanHandledCount = counts.get(TicketStatus.NEEDS_REVIEW)
				+ counts.get(TicketStatus.ESCALATED)
				+ counts.get(TicketStatus.PENDING_CUSTOMER)
				+ counts.get(TicketStatus.RESOLVED)
				+ counts.get(TicketStatus.CLOSED);

		List<StatusCount> byStatus = new ArrayList<>();
		for (TicketStatus status : STATUS_ORDER) {
			byStatus.add(new StatusCount(status, counts.get(status)));
		}

		List<CategoryCount> byCategory = new ArrayList<>();
		for (CategoryCountRow row : ticketRepository.countByCategoryGrouped()) {
			byCategory.add(new CategoryCount(row.getCategory(), row.getCount()));
		}

		return new DashboardMetricsResponse(
				totalTickets, resolutionRatePercent, aiHandledCount, humanHandledCount, byStatus, byCategory);
	}
}
