package com.supportsystem.backend.ticket;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.supportsystem.backend.category.EmailCategorizationService;
import com.supportsystem.backend.email.EmailMessage;
import com.supportsystem.backend.email.EmailSendException;
import com.supportsystem.backend.email.EmailSender;
import com.supportsystem.backend.reply.DraftReplyResult;
import com.supportsystem.backend.reply.DraftReplyService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TicketService {

	private static final Set<TicketStatus> REOPEN_ON_INBOUND = EnumSet.of(
			TicketStatus.RESOLVED, TicketStatus.CLOSED, TicketStatus.PENDING_CUSTOMER);

	private final TicketRepository ticketRepository;
	private final TicketMessageRepository ticketMessageRepository;
	private final EmailSender emailSender;
	private final EmailCategorizationService emailCategorizationService;
	private final DraftReplyService draftReplyService;
	private final String inboundReplyDomain;

	public TicketService(TicketRepository ticketRepository,
			TicketMessageRepository ticketMessageRepository,
			EmailSender emailSender,
			EmailCategorizationService emailCategorizationService,
			DraftReplyService draftReplyService,
			@Value("${sendgrid.inbound-reply-domain}") String inboundReplyDomain) {
		this.ticketRepository = ticketRepository;
		this.ticketMessageRepository = ticketMessageRepository;
		this.emailSender = emailSender;
		this.emailCategorizationService = emailCategorizationService;
		this.draftReplyService = draftReplyService;
		this.inboundReplyDomain = inboundReplyDomain;
	}

	@Transactional
	public void processInboundEmail(String fromEmail, String fromName, String subject,
			String mailboxHash, String body, String providerMessageId) {
		if (ticketMessageRepository.existsByProviderMessageId(providerMessageId)) {
			log.info("Duplicate inbound webhook delivery ignored: messageId={}", providerMessageId);
			return;
		}

		Ticket existingTicket = findTicketByMailboxHash(mailboxHash);
		boolean matchedExisting = existingTicket != null;
		Ticket ticket = matchedExisting
				? reopenIfNeeded(existingTicket)
				: createTicket(fromEmail, fromName, subject, body);

		TicketMessage message = new TicketMessage();
		message.setTicket(ticket);
		message.setDirection(MessageDirection.INBOUND);
		message.setSenderEmail(fromEmail);
		message.setBody(body);
		message.setProviderMessageId(providerMessageId);

		try {
			ticketMessageRepository.save(message);
		} catch (DataIntegrityViolationException e) {
			log.info("Duplicate inbound webhook delivery ignored on race: messageId={}", providerMessageId);
			return;
		}

		ticket.setLastMessageAt(message.getCreatedAt());
		ticketRepository.save(ticket);

		log.info("Inbound email processed: ticketId={}, messageId={}, matchedExisting={}, bodyLength={}",
				ticket.getId(), message.getId(), matchedExisting, body.length());

		if (!matchedExisting) {
			attemptAiReply(ticket, subject, body);
		}
	}

	private void attemptAiReply(Ticket ticket, String subject, String body) {
		DraftReplyResult result = draftReplyService.generateDraft(subject, body);
		if (result.answerable()) {
			try {
				sendAdminReply(ticket.getId(), result.reply());
				ticket.setStatus(TicketStatus.AI_RESPONDED);
				ticketRepository.save(ticket);
				log.info("AI auto-replied to new ticket {}", ticket.getId());
				return;
			} catch (EmailSendException e) {
				log.warn("AI auto-reply send failed for ticket {}, escalating instead", ticket.getId(), e);
			}
		}
		ticket.setStatus(TicketStatus.ESCALATED);
		ticketRepository.save(ticket);
		log.info("Ticket {} escalated (no confident AI answer)", ticket.getId());
	}

	private Ticket reopenIfNeeded(Ticket ticket) {
		if (REOPEN_ON_INBOUND.contains(ticket.getStatus())) {
			ticket.setStatus(TicketStatus.NEW);
		}
		return ticket;
	}

	private Ticket createTicket(String fromEmail, String fromName, String subject, String body) {
		Ticket ticket = new Ticket();
		ticket.setCustomerEmail(fromEmail);
		ticket.setCustomerName(fromName);
		ticket.setSubject(subject);
		ticket.setStatus(TicketStatus.NEW);
		ticket.setCategory(emailCategorizationService.categorize(subject, body));
		return ticketRepository.save(ticket);
	}

	private Ticket findTicketByMailboxHash(String mailboxHash) {
		if (mailboxHash == null || mailboxHash.isBlank()) {
			return null;
		}
		try {
			Long ticketId = Long.parseLong(mailboxHash.trim());
			return ticketRepository.findById(ticketId).orElse(null);
		} catch (NumberFormatException e) {
			log.warn("Unmatched mailboxHash, creating new ticket instead");
			return null;
		}
	}

	public Page<Ticket> listTickets(TicketStatus status, String category, Pageable pageable) {
		if (status != null && category != null) {
			return ticketRepository.findByStatusAndCategory(status, category, pageable);
		}
		if (status != null) {
			return ticketRepository.findByStatus(status, pageable);
		}
		if (category != null) {
			return ticketRepository.findByCategory(category, pageable);
		}
		return ticketRepository.findAll(pageable);
	}

	public List<String> listCategories() {
		return ticketRepository.findDistinctCategories();
	}

	public Ticket getTicket(Long ticketId) {
		return ticketRepository.findById(ticketId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
	}

	public List<TicketMessage> getMessages(Long ticketId) {
		return ticketMessageRepository.findByTicket_IdOrderByCreatedAtAsc(ticketId);
	}

	@Transactional
	public TicketMessage sendAdminReply(Long ticketId, String body) {
		Ticket ticket = getTicket(ticketId);

		String replyTo = "support+" + ticket.getId() + "@" + inboundReplyDomain;
		String subject = ticket.getSubject().startsWith("Re:") ? ticket.getSubject() : "Re: " + ticket.getSubject();

		emailSender.send(new EmailMessage(ticket.getCustomerEmail(), subject, body, replyTo));

		TicketMessage message = new TicketMessage();
		message.setTicket(ticket);
		message.setDirection(MessageDirection.OUTBOUND);
		message.setSenderEmail(replyTo);
		message.setBody(body);
		TicketMessage saved = ticketMessageRepository.save(message);

		ticket.setLastMessageAt(saved.getCreatedAt());
		ticketRepository.save(ticket);

		return saved;
	}

	@Transactional
	public Ticket updateStatus(Long ticketId, TicketStatus status) {
		Ticket ticket = getTicket(ticketId);
		ticket.setStatus(status);
		return ticketRepository.save(ticket);
	}
}
