package com.supportsystem.backend.email;

public record EmailMessage(String to, String subject, String textBody, String replyTo) {
}
