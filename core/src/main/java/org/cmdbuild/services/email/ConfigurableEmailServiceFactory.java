package org.cmdbuild.services.email;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.Email;

import com.google.common.base.Supplier;

public class ConfigurableEmailServiceFactory implements EmailServiceFactory {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConfigurableEmailServiceFactory> {

		private MailApiFactory apiFactory;
		private Store<Email> store;
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
			Validate.notNull(store, "missing '%s'", Store.class);
			Validate.notNull(accountSupplier, "missing '%s' supplier", EmailAccount.class);
		};

		public Builder withApiFactory(final MailApiFactory apiFactory) {
			this.apiFactory = apiFactory;
			return this;
		}

		public Builder withPersistence(final Store<Email> store) {
			this.store = store;
			return this;
		}

		public Builder withDefaultAccountSupplier(final Supplier<EmailAccount> accountSupplier) {
			this.accountSupplier = accountSupplier;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final MailApiFactory apiFactory;
	private final Store<Email> store;
	private final Supplier<EmailAccount> accountSupplier;

	public ConfigurableEmailServiceFactory(final Builder builder) {
		this.apiFactory = builder.apiFactory;
		this.store = builder.store;
		this.accountSupplier = builder.accountSupplier;
	}

	@Override
	public EmailService create() {
		return create(accountSupplier);
	}

	@Override
	public EmailService create(final Supplier<EmailAccount> emailAccountSupplier) {
		Validate.notNull(emailAccountSupplier, "missing '%s'", EmailAccount.class);
		return new DefaultEmailService(emailAccountSupplier, apiFactory, store);
	}

}
