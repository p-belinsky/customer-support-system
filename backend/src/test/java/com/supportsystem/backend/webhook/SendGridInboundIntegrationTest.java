package com.supportsystem.backend.webhook;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.supportsystem.backend.reply.DraftReplyResult;
import com.supportsystem.backend.reply.DraftReplyService;
import com.supportsystem.backend.ticket.Ticket;
import com.supportsystem.backend.ticket.TicketRepository;
import com.supportsystem.backend.ticket.TicketStatus;

@SpringBootTest(properties = "sendgrid.webhook.secret=test-secret")
@AutoConfigureMockMvc
@Transactional
class SendGridInboundIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TicketRepository ticketRepository;

	@MockitoBean
	private DraftReplyService draftReplyService;

	@BeforeEach
	void setUp() {
		when(draftReplyService.generateDraft(any(), any())).thenReturn(DraftReplyResult.unanswerable());
	}

	@Test
	void missingTokenIsUnauthorized() throws Exception {
		mockMvc.perform(multipart("/api/webhooks/sendgrid/inbound")
						.param("from", "customer@example.com")
						.param("subject", "Help")
						.param("text", "body"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void wrongTokenIsUnauthorized() throws Exception {
		mockMvc.perform(multipart("/api/webhooks/sendgrid/inbound")
						.param("token", "wrong-secret")
						.param("from", "customer@example.com")
						.param("subject", "Help")
						.param("text", "body"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void newMailboxHashCreatesNewTicket() throws Exception {
		String envelope = "{\"to\":[\"support+999999@inbound.example.com\"],\"from\":\"customer@example.com\"}";

		mockMvc.perform(multipart("/api/webhooks/sendgrid/inbound")
						.param("token", "test-secret")
						.param("from", "\"New Customer\" <newcustomer@example.com>")
						.param("subject", "Help me")
						.param("text", "I need help")
						.param("envelope", envelope)
						.param("headers", "Message-ID: <unique-1@example.com>"))
				.andExpect(status().isOk());

		Ticket ticket = ticketRepository.findAll().stream()
				.filter(t -> "newcustomer@example.com".equals(t.getCustomerEmail()))
				.findFirst()
				.orElseThrow();
		assertThat(ticket.getStatus()).isEqualTo(TicketStatus.ESCALATED);
		assertThat(ticket.getCustomerName()).isEqualTo("New Customer");
	}

	@Test
	void matchingMailboxHashReopensResolvedTicket() throws Exception {
		Ticket resolved = new Ticket();
		resolved.setCustomerEmail("existing@example.com");
		resolved.setSubject("Old issue");
		resolved.setStatus(TicketStatus.RESOLVED);
		resolved = ticketRepository.save(resolved);

		String envelope = "{\"to\":[\"support+" + resolved.getId() + "@inbound.example.com\"],\"from\":\"existing@example.com\"}";

		mockMvc.perform(multipart("/api/webhooks/sendgrid/inbound")
						.param("token", "test-secret")
						.param("from", "existing@example.com")
						.param("subject", "Re: Old issue")
						.param("text", "Still broken")
						.param("envelope", envelope)
						.param("headers", "Message-ID: <unique-2@example.com>"))
				.andExpect(status().isOk());

		Ticket reopened = ticketRepository.findById(resolved.getId()).orElseThrow();
		assertThat(reopened.getStatus()).isEqualTo(TicketStatus.NEW);
	}

	@Test
	void duplicateMessageIdIsIgnored() throws Exception {
		String envelope = "{\"to\":[\"support+888888@inbound.example.com\"],\"from\":\"dup@example.com\"}";

		for (int i = 0; i < 2; i++) {
			mockMvc.perform(multipart("/api/webhooks/sendgrid/inbound")
							.param("token", "test-secret")
							.param("from", "dup@example.com")
							.param("subject", "Dup test")
							.param("text", "body")
							.param("envelope", envelope)
							.param("headers", "Message-ID: <dup-message@example.com>"))
					.andExpect(status().isOk());
		}

		long matching = ticketRepository.findAll().stream()
				.filter(t -> "dup@example.com".equals(t.getCustomerEmail()))
				.count();
		assertThat(matching).isEqualTo(1);
	}
}
