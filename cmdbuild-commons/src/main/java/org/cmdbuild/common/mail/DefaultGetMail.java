package org.cmdbuild.common.mail;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.common.Builder;

import com.google.common.collect.Lists;

class DefaultGetMail implements GetMail {

	public static class DefaultGetMailBuilder implements Builder<DefaultGetMail> {

		private String id;
		private String folder;
		private String subject;
		private String content;
		private List<Attachment> attachments = Collections.emptyList();

		private DefaultGetMailBuilder() {
			// prevents instantiation
		}

		@Override
		public DefaultGetMail build() {
			return new DefaultGetMail(this);
		}

		public DefaultGetMailBuilder withId(final String id) {
			this.id = id;
			return this;
		}

		public DefaultGetMailBuilder withFolder(final String folder) {
			this.folder = folder;
			return this;
		}

		public DefaultGetMailBuilder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public DefaultGetMailBuilder withContent(final String content) {
			this.content = content;
			return this;
		}

		public DefaultGetMailBuilder withAttachments(final Iterable<Attachment> attachments) {
			this.attachments = Lists.newArrayList(attachments);
			return this;
		}

	}

	public static DefaultGetMailBuilder newInstance() {
		return new DefaultGetMailBuilder();
	}

	private final String id;
	private final String folder;
	private final String subject;
	private final String content;
	private final Iterable<Attachment> attachments;

	public DefaultGetMail(final DefaultGetMailBuilder builder) {
		this.id = builder.id;
		this.folder = builder.folder;
		this.subject = builder.subject;
		this.content = builder.content;
		this.attachments = builder.attachments;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getFolder() {
		return folder;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getContent() {
		return content;
	}

	@Override
	public Iterable<Attachment> getAttachments() {
		return attachments;
	}

}
