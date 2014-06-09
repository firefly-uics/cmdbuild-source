package org.cmdbuild.data.store.email;

import javax.activation.DataHandler;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Attachment {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<Attachment> {

		private String name;
		private DataHandler dataHandler;

		private Builder() {
			// prevents instantiation
		}

		@Override
		public Attachment build() {
			return new Attachment(this);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDataHandler(final DataHandler dataHandler) {
			this.dataHandler = dataHandler;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final DataHandler dataHandler;

	private Attachment(final Builder builder) {
		this.name = builder.name;
		this.dataHandler = builder.dataHandler;
	}

	public String getName() {
		return name;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", name) //
				.toString();
	}

}
