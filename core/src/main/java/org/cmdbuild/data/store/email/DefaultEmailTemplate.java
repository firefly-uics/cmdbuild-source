package org.cmdbuild.data.store.email;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DefaultEmailTemplate implements EmailTemplate {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<DefaultEmailTemplate> {

		private Long id;
		private String name;
		private String description;
		private String from;
		private String to;
		private String cc;
		private String bcc;
		private String subject;
		private String body;
		private Long account;

		private Builder() {
			// use factory method
		}

		@Override
		public DefaultEmailTemplate build() {
			validate();
			return new DefaultEmailTemplate(this);
		}

		private void validate() {
			Validate.isTrue(isNotBlank(name), "invalid name");
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withFrom(final String from) {
			this.from = from;
			return this;
		}

		public Builder withTo(final String to) {
			this.to = to;
			return this;
		}

		public Builder withCc(final String cc) {
			this.cc = cc;
			return this;
		}

		public Builder withBcc(final String bcc) {
			this.bcc = bcc;
			return this;
		}

		public Builder withSubject(final String subject) {
			this.subject = subject;
			return this;
		}

		public Builder withBody(final String body) {
			this.body = body;
			return this;
		}

		public Builder withAccount(final Long account) {
			this.account = account;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String name;
	private final String description;
	private final String from;
	private final String to;
	private final String cc;
	private final String bcc;
	private final String subject;
	private final String body;
	private final Long account;

	private DefaultEmailTemplate(final Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.from = builder.from;
		this.to = builder.to;
		this.cc = builder.cc;
		this.bcc = builder.bcc;
		this.subject = builder.subject;
		this.body = builder.body;
		this.account = builder.account;
	}

	@Override
	public String getIdentifier() {
		return this.getName();
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getFrom() {
		return from;
	}

	@Override
	public String getTo() {
		return to;
	}

	/**
	 * Read the "TO" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	@Override
	public List<String> getToAddresses() {
		return Arrays.asList(getTo().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	@Override
	public String getCc() {
		return cc;
	}

	/**
	 * Read the "CC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	@Override
	public List<String> getCCAddresses() {
		return Arrays.asList(getCc().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	@Override
	public String getBcc() {
		return bcc;
	}

	/**
	 * Read the "BCC" attribute and build a list splitting over the separator of
	 * email addresses {@link EmailConstants.ADDRESSES_SEPARATOR}.
	 */
	@Override
	public List<String> getBCCAddresses() {
		return Arrays.asList(getBcc().split(EmailConstants.ADDRESSES_SEPARATOR));
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getBody() {
		return body;
	}

	@Override
	public Long getAccount() {
		return account;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
