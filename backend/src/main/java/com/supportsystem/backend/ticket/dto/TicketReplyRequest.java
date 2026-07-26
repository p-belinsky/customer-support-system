package com.supportsystem.backend.ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record TicketReplyRequest(@NotBlank String body) {
}
