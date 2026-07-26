package com.supportsystem.backend.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse;

class TicketControllerTest {

	@Test
	void metricsDelegatesToTicketMetricsService() {
		TicketService ticketService = mock(TicketService.class);
		TicketMetricsService ticketMetricsService = mock(TicketMetricsService.class);
		DashboardMetricsResponse expected = new DashboardMetricsResponse(0, null, 0, 0, List.of(), List.of());
		when(ticketMetricsService.computeMetrics()).thenReturn(expected);

		TicketController controller = new TicketController(ticketService, ticketMetricsService);

		assertThat(controller.metrics()).isSameAs(expected);
	}
}
