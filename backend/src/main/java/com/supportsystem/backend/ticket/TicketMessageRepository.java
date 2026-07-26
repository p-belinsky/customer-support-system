package com.supportsystem.backend.ticket;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {

	List<TicketMessage> findByTicket_IdOrderByCreatedAtAsc(Long ticketId);

	boolean existsByProviderMessageId(String providerMessageId);
}
