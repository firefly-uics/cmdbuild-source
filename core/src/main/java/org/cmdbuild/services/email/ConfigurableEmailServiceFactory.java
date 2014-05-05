package org.cmdbuild.services.email;

import static com.google.common.base.Suppliers.ofInstance;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.api.mail.MailApiFactory;

import com.google.common.base.Supplier;

public class ConfigurableEmailServiceFactory implements EmailServiceFactory {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConfigurableEmailServiceFactory> {

		private MailApiFactory apiFactory;
		private EmailPersistence persistence;
		private Supplier<EmailAccount> accountSupplier;

		private Builder() {
			// use factory method
		}

		@Override
		public ConfigurableEmailServiceFactory build() {
			validate();
			return new ConfigurableEmailServiceFactory(this);
		}

		private void validate() {
			Validate.notNull(apiFactory, "missing '%s'", MailApiFactory.class);
			Validate.notNull(persistence, "missing '%s'", EmailPersistence.class);
			Validate.notNull(accountSupplier, "missing '%s' supplier", EmailAccount.class);
		};

		public Builder withApiFactory(final MailApiFactory apiFactory) {
			this.apiFactory = apiFactory;
			return this;
		}

		public Builder withPersistence(final EmailPersistence persistence) {
			this.persistence = persistence;
			return this;
		}

		public Builder withConfiguration(final Supplier<EmailAccount> accountSupplier) {
			this.accountSupplier = accountSupplier;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final MailApiFactory apiFactory;
	private final EmailPersistence persistence;
	private final Supplier<EmailAccount> accountSupplier;

	public ConfigurableEmailServiceFactory(final Builder builder) {
		this.apiFactory = builder.apiFactory;
		this.persistence = builder.persistence;
		this.accountSupplier = builder.accountSupplier;
	}

	@Override
	public EmailService create() {
		return create(accountSupplier.get());
	}

	@Override
	public EmailService create(final EmailAccount account) {
		Validate.notNull(account, "missing '%s'", EmailAccount.class);
		return new DefaultEmailService(ofInstance(account), apiFactory, persistence);
	}

}
