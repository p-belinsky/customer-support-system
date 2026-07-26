package com.supportsystem.backend.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.supportsystem.backend.ticket.TicketRepository.CategoryCountRow;
import com.supportsystem.backend.ticket.TicketRepository.StatusCountRow;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse.CategoryCount;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse.StatusCount;

class TicketMetricsServiceTest {

	private final TicketRepository ticketRepository = mock(TicketRepository.class);
	private final TicketMetricsService service = new TicketMetricsService(ticketRepository);

	@Test
	void zeroTicketsProducesNullRatesAndZeroFilledStatuses() {
		when(ticketRepository.countByStatusGrouped()).thenReturn(List.of());
		when(ticketRepository.countByCategoryGrouped()).thenReturn(List.of());

		DashboardMetricsResponse result = service.computeMetrics();

		assertThat(result.totalTickets()).isZero();
		assertThat(result.resolutionRatePercent()).isNull();
		assertThat(result.aiHandledCount()).isZero();
		assertThat(result.humanHandledCount()).isZero();
		assertThat(result.byStatus()).hasSize(7).allSatisfy(sc -> assertThat(sc.count()).isZero());
		assertThat(result.byCategory()).isEmpty();
	}

	@Test
	void mixedStatusesComputeResolutionRateAndHandledCounts() {
		List<StatusCountRow> statusRows = List.of(
				statusRow(TicketStatus.NEW, 1),
				statusRow(TicketStatus.AI_RESPONDED, 2),
				statusRow(TicketStatus.NEEDS_REVIEW, 1),
				statusRow(TicketStatus.ESCALATED, 1),
				statusRow(TicketStatus.RESOLVED, 3),
				statusRow(TicketStatus.CLOSED, 2));
		List<CategoryCountRow> categoryRows = List.of(
				categoryRow("Billing", 4),
				categoryRow("Uncategorized", 6));
		when(ticketRepository.countByStatusGrouped()).thenReturn(statusRows);
		when(ticketRepository.countByCategoryGrouped()).thenReturn(categoryRows);

		DashboardMetricsResponse result = service.computeMetrics();

		assertThat(result.totalTickets()).isEqualTo(10);
		assertThat(result.resolutionRatePercent()).isEqualTo(50.0);
		assertThat(result.aiHandledCount()).isEqualTo(2);
		assertThat(result.humanHandledCount()).isEqualTo(7);
		assertThat(result.byStatus())
				.extracting(StatusCount::status)
				.containsExactly(
						TicketStatus.NEW, TicketStatus.AI_RESPONDED, TicketStatus.NEEDS_REVIEW,
						TicketStatus.ESCALATED, TicketStatus.PENDING_CUSTOMER, TicketStatus.RESOLVED,
						TicketStatus.CLOSED);
		assertThat(result.byCategory())
				.containsExactly(new CategoryCount("Billing", 4), new CategoryCount("Uncategorized", 6));
	}

	private static StatusCountRow statusRow(TicketStatus status, long count) {
		StatusCountRow row = mock(StatusCountRow.class);
		when(row.getStatus()).thenReturn(status);
		when(row.getCount()).thenReturn(count);
		return row;
	}

	private static CategoryCountRow categoryRow(String category, long count) {
		CategoryCountRow row = mock(CategoryCountRow.class);
		when(row.getCategory()).thenReturn(category);
		when(row.getCount()).thenReturn(count);
		return row;
	}
}
