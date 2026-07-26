package com.supportsystem.backend.email;

public interface EmailSender {

	EmailSendResult send(EmailMessage message);
}
