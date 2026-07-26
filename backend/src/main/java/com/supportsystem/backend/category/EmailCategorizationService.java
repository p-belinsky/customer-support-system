package com.supportsystem.backend.category;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EmailCategorizationService {

	private static final String CATEGORY_LIST = Arrays.stream(TicketCategory.values())
			.map(TicketCategory::getLabel)
			.collect(Collectors.joining(", "));

	private final ChatClient chatClient;

	public EmailCategorizationService(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	public String categorize(String subject, String body) {
		try {
			CategorizationResult result = chatClient.prompt()
					.user(u -> u.text("""
							Classify this customer support email into exactly one of these categories: {categories}.

							Subject: {subject}
							Body: {body}
							""")
							.param("categories", CATEGORY_LIST)
							.param("subject", subject)
							.param("body", body))
					.call()
					.entity(CategorizationResult.class);

			String rawCategory = result == null ? null : result.category();
			TicketCategory category = rawCategory == null ? null : TicketCategory.fromLabel(rawCategory.trim());
			if (category == null) {
				log.warn("AI returned unrecognized category '{}', falling back to General", rawCategory);
				return TicketCategory.GENERAL.getLabel();
			}
			log.info("Email categorized as '{}'", category.getLabel());
			return category.getLabel();
		} catch (Exception e) {
			log.warn("Email categorization failed, falling back to General", e);
			return TicketCategory.GENERAL.getLabel();
		}
	}

	private record CategorizationResult(String category) {
	}
}
