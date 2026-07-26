package com.supportsystem.backend.ticket.dto;

import jakarta.validation.constraints.NotNull;

import com.supportsystem.backend.ticket.TicketStatus;

public record TicketStatusUpdateRequest(@NotNull TicketStatus status) {
}
