package com.supportsystem.backend.ticket;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.supportsystem.backend.email.EmailSendException;
import com.supportsystem.backend.ticket.dto.DashboardMetricsResponse;
import com.supportsystem.backend.ticket.dto.TicketDetailResponse;
import com.supportsystem.backend.ticket.dto.TicketListResponse;
import com.supportsystem.backend.ticket.dto.TicketMessageResponse;
import com.supportsystem.backend.ticket.dto.TicketReplyRequest;
import com.supportsystem.backend.ticket.dto.TicketStatusUpdateRequest;
import com.supportsystem.backend.ticket.dto.TicketSummaryResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

	private final TicketService ticketService;
	private final TicketMetricsService ticketMetricsService;

	public TicketController(TicketService ticketService, TicketMetricsService ticketMetricsService) {
		this.ticketService = ticketService;
		this.ticketMetricsService = ticketMetricsService;
	}

	@GetMapping
	public TicketListResponse list(
			@RequestParam(required = false) TicketStatus status,
			@RequestParam(required = false) String category,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		PageRequest pageRequest = PageRequest.of(page, size, Sort.by("lastMessageAt").descending());
		Page<Ticket> tickets = ticketService.listTickets(status, category, pageRequest);
		return TicketListResponse.from(tickets);
	}

	@GetMapping("/categories")
	public List<String> categories() {
		return ticketService.listCategories();
	}

	@GetMapping("/metrics")
	public DashboardMetricsResponse metrics() {
		return ticketMetricsService.computeMetrics();
	}

	@GetMapping("/{id}")
	public TicketDetailResponse detail(@PathVariable Long id) {
		Ticket ticket = ticketService.getTicket(id);
		return TicketDetailResponse.from(ticket, ticketService.getMessages(id));
	}

	@PostMapping("/{id}/messages")
	public ResponseEntity<TicketMessageResponse> reply(@PathVariable Long id, @Valid @RequestBody TicketReplyRequest request) {
		TicketMessage message = ticketService.sendAdminReply(id, request.body());
		return ResponseEntity.status(HttpStatus.CREATED).body(TicketMessageResponse.from(message));
	}

	@ExceptionHandler(EmailSendException.class)
	public ResponseEntity<String> handleEmailSendFailure(EmailSendException e) {
		return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to send email");
	}
}
