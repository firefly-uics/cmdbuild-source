package org.cmdbuild.logic.email;

import static com.google.common.collect.Lists.newArrayList;

import java.io.IOException;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.Logic;

public interface EmailAttachmentsLogic extends Logic {

	public static class Upload {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Upload> {

			private String identifier;
			private DataHandler dataHandler;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public Upload build() {
				validate();
				return new Upload(this);
			}

			private void validate() {
				Validate.notNull(dataHandler, "invalid data handler");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withDataHandler(final DataHandler dataHandler) {
				this.dataHandler = dataHandler;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder newUpload() {
			return new Builder();
		}

		public String identifier;
		public DataHandler dataHandler;
		public boolean temporary;

		private Upload(final Builder builder) {
			this.identifier = builder.identifier;
			this.dataHandler = builder.dataHandler;
			this.temporary = builder.temporary;
		}

	}

	public static class Delete {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Delete> {

			private String identifier;
			private String fileName;
			private boolean temporary;

			private Builder() {
				// prevents direct instantiation
			}

			@Override
			public Delete build() {
				validate();
				return new Delete(this);
			}

			private void validate() {
				Validate.notNull(fileName, "invalid file name");
				Validate.notEmpty(fileName, "invalid file name");
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withFileName(final String fileName) {
				this.fileName = fileName;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

		}

		public static Builder newDelete() {
			return new Builder();
		}

		public String identifier;
		public String fileName;
		public boolean temporary;

		private Delete(final Builder builder) {
			this.identifier = builder.identifier;
			this.fileName = builder.fileName;
			this.temporary = builder.temporary;
		}

	}

	public static class CopiableAttachment {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<CopiableAttachment> {

			private String className;
			private Long cardId;
			private String fileName;

			private Builder() {
				// prevents instantiation
			}

			@Override
			public CopiableAttachment build() {
				return new CopiableAttachment(this);
			}

			public Builder withClassName(final String className) {
				this.className = className;
				return this;
			}

			public Builder withCardId(final Long cardId) {
				this.cardId = cardId;
				return this;
			}

			public Builder withFileName(final String fileName) {
				this.fileName = fileName;
				return this;
			}

		}

		public static Builder newCopy() {
			return new Builder();
		}

		public String className;
		public Long cardId;
		public String fileName;

		private CopiableAttachment(final Builder builder) {
			this.className = builder.className;
			this.cardId = builder.cardId;
			this.fileName = builder.fileName;
		}

	}

	public static class Copy {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Copy> {

			private String identifier;
			private boolean temporary;
			private Iterable<CopiableAttachment> attachments;

			private Builder() {
				// prevents direct instantiation
				attachments = newArrayList();
			}

			@Override
			public Copy build() {
				return new Copy(this);
			}

			public Builder withIdentifier(final String identifier) {
				this.identifier = identifier;
				return this;
			}

			public Builder withTemporaryStatus(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

			public Builder withAllAttachments(final Iterable<CopiableAttachment> attachments) {
				this.attachments = attachments;
				return this;
			}

		}

		public static Builder newCopy() {
			return new Builder();
		}

		public String identifier;
		public boolean temporary;
		private final Iterable<CopiableAttachment> attachments;

		private Copy(final Builder builder) {
			this.identifier = builder.identifier;
			this.temporary = builder.temporary;
			this.attachments = builder.attachments;
		}

		public Builder modify() {
			return newCopy().withIdentifier(identifier).withTemporaryStatus(temporary).withAllAttachments(attachments);
		}

		public Iterable<CopiableAttachment> getAttachments() {
			return attachments;
		}

	}

	void uploadAttachment(Upload upload) throws IOException, CMDBException;

	void deleteAttachment(Delete delete) throws CMDBException;

	Copy copyAttachments(Copy copy) throws CMDBException;

}
