package org.cmdbuild.model.email;

import java.net.URL;

import org.cmdbuild.common.Builder;

public class Attachment {

	public static class AttachmentBuilder implements Builder<Attachment> {

		private String name;
		private URL url;

		private AttachmentBuilder() {
			// prevents instantiation
		}

		@Override
		public Attachment build() {
			return new Attachment(this);
		}

		public AttachmentBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public AttachmentBuilder withUrl(final URL url) {
			this.url = url;
			return this;
		}

	}

	public static AttachmentBuilder newInstance() {
		return new AttachmentBuilder();
	}

	private final String name;
	private final URL url;

	public Attachment(final AttachmentBuilder builder) {
		this.name = builder.name;
		this.url = builder.url;
	}

	public String getName() {
		return name;
	}

	public URL getUrl() {
		return url;
	}

}
