package com.supportsystem.backend.reply;

public record DraftReplyResult(boolean answerable, String reply) {

	public static DraftReplyResult unanswerable() {
		return new DraftReplyResult(false, null);
	}
}
