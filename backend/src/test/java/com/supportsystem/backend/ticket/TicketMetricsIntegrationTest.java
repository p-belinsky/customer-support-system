package com.supportsystem.backend.ticket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * Assertions compare metrics before/after seeding rather than exact absolute values,
 * since these queries aggregate over the whole tickets table and this suite runs
 * against the real shared local Postgres instance (no per-test DB isolation).
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketMetricsIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TicketRepository ticketRepository;

	@Test
	void metricsIsUnauthorizedWithoutSession() throws Exception {
		mockMvc.perform(get("/api/tickets/metrics")).andExpect(status().isUnauthorized());
	}

	@Test
	void metricsReflectsNewlySeededTickets() throws Exception {
		MockHttpSession session = login();
		String before = fetchMetrics(session);

		seedTicket(TicketStatus.NEW, "Technical");
		seedTicket(TicketStatus.AI_RESPONDED, "Billing");
		seedTicket(TicketStatus.NEEDS_REVIEW, "Billing");
		seedTicket(TicketStatus.ESCALATED, null);
		seedTicket(TicketStatus.RESOLVED, "Technical");

		String after = fetchMetrics(session);

		assertThat(longAt(after, "$.totalTickets") - longAt(before, "$.totalTickets")).isEqualTo(5);
		assertThat(longAt(after, "$.aiHandledCount") - longAt(before, "$.aiHandledCount")).isEqualTo(1);
		assertThat(longAt(after, "$.humanHandledCount") - longAt(before, "$.humanHandledCount")).isEqualTo(3);
		assertThat(statusCount(after, "NEW") - statusCount(before, "NEW")).isEqualTo(1);
		assertThat(statusCount(after, "RESOLVED") - statusCount(before, "RESOLVED")).isEqualTo(1);
		assertThat((List<?>) JsonPath.read(after, "$.byStatus")).hasSize(7);
		assertThat((List<?>) JsonPath.read(after, "$.byCategory[?(@.category=='Billing')]")).hasSize(1);
	}

	private long longAt(String json, String path) {
		return ((Number) JsonPath.read(json, path)).longValue();
	}

	private long statusCount(String json, String status) {
		List<?> matches = JsonPath.read(json, "$.byStatus[?(@.status=='" + status + "')].count");
		return ((Number) matches.get(0)).longValue();
	}

	private String fetchMetrics(MockHttpSession session) throws Exception {
		return mockMvc.perform(get("/api/tickets/metrics").session(session))
				.andExpect(status().isOk())
				.andReturn()
				.getResponse()
				.getContentAsString();
	}

	private MockHttpSession login() throws Exception {
		MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
						.param("username", "admin")
						.param("password", "admin123"))
				.andExpect(status().isOk())
				.andReturn();
		return (MockHttpSession) loginResult.getRequest().getSession(false);
	}

	private void seedTicket(TicketStatus status, String category) {
		Ticket ticket = new Ticket();
		ticket.setCustomerEmail("customer@example.com");
		ticket.setSubject("Test ticket");
		ticket.setCategory(category);
		ticket.setStatus(status);
		ticketRepository.save(ticket);
	}
}
