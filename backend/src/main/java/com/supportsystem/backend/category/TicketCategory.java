package com.supportsystem.backend.category;

public enum TicketCategory {

	GENERAL("General"),
	BILLING("Billing"),
	TECHNICAL("Technical"),
	ACCOUNT("Account"),
	OTHER("Other");

	private final String label;

	TicketCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public static TicketCategory fromLabel(String label) {
		for (TicketCategory category : values()) {
			if (category.label.equalsIgnoreCase(label)) {
				return category;
			}
		}
		return null;
	}
}
