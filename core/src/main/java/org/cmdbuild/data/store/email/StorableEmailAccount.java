package org.cmdbuild.data.store.email;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.services.email.EmailAccount;

public class StorableEmailAccount implements EmailAccount, Storable {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<StorableEmailAccount> {

		private Long id;
		private String name;
		private boolean isDefault;
		private String username;
		private String password;
		private String address;
		private String smtpServer;
		private Integer smtpPort;
		private boolean smtpSsl;
		private String imapServer;
		private Integer imapPort;
		private boolean imapSsl;
		private String inputFolder;
		private String processedFolder;
		private String rejectedFolder;
		private boolean rejectNotMatching;

		private Builder() {
			// use factory method
		}

		@Override
		public StorableEmailAccount build() {
			validate();
			return new StorableEmailAccount(this);
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

		public Builder withDefaultStatus(final boolean isDefault) {
			this.isDefault = isDefault;
			return this;
		}

		public Builder withUsername(final String username) {
			this.username = username;
			return this;
		}

		public Builder withPassword(final String password) {
			this.password = password;
			return this;
		}

		public Builder withAddress(final String address) {
			this.address = address;
			return this;
		}

		public Builder withSmtpServer(final String smtpServer) {
			this.smtpServer = smtpServer;
			return this;
		}

		public Builder withSmtpPort(final Integer smtpPort) {
			this.smtpPort = smtpPort;
			return this;
		}

		public Builder withSmtpSsl(final boolean smtpSsl) {
			this.smtpSsl = smtpSsl;
			return this;
		}

		public Builder withImapServer(final String imapServer) {
			this.imapServer = imapServer;
			return this;
		}

		public Builder withImapPort(final Integer imapPort) {
			this.imapPort = imapPort;
			return this;
		}

		public Builder withImapSsl(final boolean imapSsl) {
			this.imapSsl = imapSsl;
			return this;
		}

		public Builder withInputFolder(final String inputFolder) {
			this.inputFolder = inputFolder;
			return this;
		}

		public Builder withProcessedFolder(final String processedFolder) {
			this.processedFolder = processedFolder;
			return this;
		}

		public Builder withRejectedFolder(final String rejectedFolder) {
			this.rejectedFolder = rejectedFolder;
			return this;
		}

		public Builder withRejectNotMatchingStatus(final boolean rejectNotMatching) {
			this.rejectNotMatching = rejectNotMatching;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String name;
	private final boolean isDefault;
	private final String username;
	private final String password;
	private final String address;
	private final String smtpServer;
	private final Integer smtpPort;
	private final boolean smtpSsl;
	private final String imapServer;
	private final Integer imapPort;
	private final boolean imapSsl;
	private final String inputFolder;
	private final String processedFolder;
	private final String rejectedFolder;
	private final boolean rejectNotMatching;

	private StorableEmailAccount(final Builder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.isDefault = builder.isDefault;
		this.username = builder.username;
		this.password = builder.password;
		this.address = builder.address;
		this.smtpServer = builder.smtpServer;
		this.smtpPort = builder.smtpPort;
		this.smtpSsl = builder.smtpSsl;
		this.imapServer = builder.imapServer;
		this.imapPort = builder.imapPort;
		this.imapSsl = builder.imapSsl;
		this.inputFolder = builder.inputFolder;
		this.processedFolder = builder.processedFolder;
		this.rejectedFolder = builder.rejectedFolder;
		this.rejectNotMatching = builder.rejectNotMatching;
	}

	public String getIdentifier() {
		return name;
	}

	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isDefault() {
		return isDefault;
	}

	@Override
	public String getUsername() {
		return username;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getAddress() {
		return address;
	}

	@Override
	public String getSmtpServer() {
		return smtpServer;
	}

	@Override
	public Integer getSmtpPort() {
		return smtpPort;
	}

	@Override
	public boolean isSmtpSsl() {
		return smtpSsl;
	}

	@Override
	public boolean isSmtpConfigured() {
		return isNotBlank(smtpServer) && isNotBlank(address);
	}

	@Override
	public String getImapServer() {
		return imapServer;
	}

	@Override
	public Integer getImapPort() {
		return imapPort;
	}

	@Override
	public boolean isImapSsl() {
		return imapSsl;
	}

	@Override
	public boolean isImapConfigured() {
		return isNotBlank(imapServer) && isNotBlank(username) && isNotBlank(password);
	}

	@Override
	public String getInputFolder() {
		return inputFolder;
	}

	@Override
	public String getProcessedFolder() {
		return processedFolder;
	}

	@Override
	public String getRejectedFolder() {
		return rejectedFolder;
	}

	@Override
	public boolean isRejectNotMatching() {
		return rejectNotMatching;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
