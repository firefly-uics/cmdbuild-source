package org.cmdbuild.logic.email;

import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;

public class AttachmentImpl implements Attachment {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<AttachmentImpl> {

		private String className;
		private Long cardId;
		private String fileName;

		private Builder() {
			// prevents instantiation
		}

		@Override
		public AttachmentImpl build() {
			return new AttachmentImpl(this);
		}

		public AttachmentImpl.Builder withClassName(final String className) {
			this.className = className;
			return this;
		}

		public AttachmentImpl.Builder withCardId(final Long cardId) {
			this.cardId = cardId;
			return this;
		}

		public AttachmentImpl.Builder withFileName(final String fileName) {
			this.fileName = fileName;
			return this;
		}

	}

	public static AttachmentImpl.Builder newInstance() {
		return new Builder();
	}

	private final String className;
	private final Long cardId;
	private final String fileName;

	private AttachmentImpl(final Builder builder) {
		this.className = builder.className;
		this.cardId = builder.cardId;
		this.fileName = builder.fileName;
	}

	@Override
	public String getClassName() {
		return className;
	}

	@Override
	public Long getCardId() {
		return cardId;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

}