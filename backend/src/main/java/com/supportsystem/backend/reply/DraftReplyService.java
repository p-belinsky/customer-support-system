package com.supportsystem.backend.reply;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DraftReplyService {

	private final ChatClient chatClient;
	private final String knowledgeBaseContent;

	public DraftReplyService(ChatClient.Builder chatClientBuilder,
			@Value("classpath:knowledge-base-short.md") Resource knowledgeBaseResource) {
		this.chatClient = chatClientBuilder.build();
		this.knowledgeBaseContent = readKnowledgeBase(knowledgeBaseResource);
	}

	private String readKnowledgeBase(Resource resource) {
		try {
			return resource.getContentAsString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException("Failed to load knowledge-base-short.md from classpath", e);
		}
	}

	public DraftReplyResult generateDraft(String subject, String body) {
		try {
			DraftReplyResult result = chatClient.prompt()
					.user(u -> u.text("""
							You are a customer support assistant. Answer the customer's question
							using ONLY the information in the knowledge base below. Do not use any
							outside knowledge, and do not guess or make anything up.

							If the knowledge base clearly and confidently answers the customer's
							question, set answerable=true and write a complete, polite, ready-to-send
							reply email body in the reply field (plain text, no subject line).

							If the knowledge base does not clearly cover the customer's question, or
							you are not fully confident it answers it correctly, set answerable=false
							and leave reply empty. When in doubt, choose answerable=false.

							Knowledge base:
							---
							{knowledgeBase}
							---

							Customer email subject: {subject}
							Customer email body: {body}
							""")
							.param("knowledgeBase", knowledgeBaseContent)
							.param("subject", subject)
							.param("body", body))
					.call()
					.entity(DraftReplyResult.class);

			if (result == null || !result.answerable() || result.reply() == null || result.reply().isBlank()) {
				log.info("AI could not confidently answer from knowledge base; will escalate");
				return DraftReplyResult.unanswerable();
			}
			log.info("AI generated a confident knowledge-base reply");
			return result;
		} catch (Exception e) {
			log.warn("AI draft reply generation failed, escalating instead", e);
			return DraftReplyResult.unanswerable();
		}
	}
}
