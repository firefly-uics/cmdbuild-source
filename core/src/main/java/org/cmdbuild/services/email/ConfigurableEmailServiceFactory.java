package org.cmdbuild.services.email;

import static com.google.common.base.Suppliers.ofInstance;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.api.mail.MailApiFactory;
import org.cmdbuild.config.EmailConfiguration;

import com.google.common.base.Supplier;

public class ConfigurableEmailServiceFactory implements EmailServiceFactory {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<ConfigurableEmailServiceFactory> {

		private MailApiFactory apiFactory;
		private EmailPersistence persistence;
		private Supplier<EmailConfiguration> configurationSupplier;

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
			Validate.notNull(configurationSupplier, "missing '%s' supplier", EmailConfiguration.class);
		};

		public Builder withApiFactory(final MailApiFactory apiFactory) {
			this.apiFactory = apiFactory;
			return this;
		}

		public Builder withPersistence(final EmailPersistence persistence) {
			this.persistence = persistence;
			return this;
		}

		public Builder withConfiguration(final Supplier<EmailConfiguration> configurationSupplier) {
			this.configurationSupplier = configurationSupplier;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final MailApiFactory apiFactory;
	private final EmailPersistence persistence;
	private final Supplier<EmailConfiguration> configurationSupplier;

	public ConfigurableEmailServiceFactory(final Builder builder) {
		this.apiFactory = builder.apiFactory;
		this.persistence = builder.persistence;
		this.configurationSupplier = builder.configurationSupplier;
	}

	@Override
	public EmailService create() {
		return create(configurationSupplier.get());
	}

	@Override
	public EmailService create(final EmailConfiguration configuration) {
		Validate.notNull(apiFactory, "null api factory");
		Validate.notNull(persistence, "null persistence");
		Validate.notNull(configuration, "null configuration");
		return new DefaultEmailService(ofInstance(configuration), apiFactory, persistence);
	}

}
