package org.cmdbuild.logic.email;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.template.TemplateResolvers.identity;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;

import com.google.common.base.Supplier;

public class SendTemplateEmail implements Action {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SendTemplateEmail> {

		private Supplier<EmailAccount> emailAccoutSupplier;
		private EmailServiceFactory emailServiceFactory;
		private Supplier<Template> emailTemplateSupplier;
		private TemplateResolver templateResolver;

		private Builder() {
			// use factory method
		}

		@Override
		public SendTemplateEmail build() {
			validate();
			return new SendTemplateEmail(this);
		}

		private void validate() {
			Validate.notNull(emailAccoutSupplier, "missing '%s' supplier", EmailAccount.class);
			Validate.notNull(emailServiceFactory, "missing '%s'", EmailServiceFactory.class);
			Validate.notNull(emailTemplateSupplier, "missing '%s' supplier", Template.class);

			templateResolver = defaultIfNull(templateResolver, identity());
		}

		public Builder withEmailAccountSupplier(final Supplier<EmailAccount> emailAccoutSupplier) {
			this.emailAccoutSupplier = emailAccoutSupplier;
			return this;
		}

		public Builder withEmailServiceFactory(final EmailServiceFactory emailServiceFactory) {
			this.emailServiceFactory = emailServiceFactory;
			return this;
		}

		public Builder withEmailTemplateSupplier(final Supplier<Template> emailTemplateSupplier) {
			this.emailTemplateSupplier = emailTemplateSupplier;
			return this;
		}

		public Builder withTemplateResolver(final TemplateResolver templateResolver) {
			this.templateResolver = templateResolver;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Supplier<EmailAccount> emailAccoutSupplier;
	private final EmailServiceFactory emailServiceFactory;
	private final Supplier<Template> emailTemplateSupplier;
	private final TemplateResolver templateResolver;

	private SendTemplateEmail(final Builder builder) {
		this.emailAccoutSupplier = builder.emailAccoutSupplier;
		this.emailServiceFactory = builder.emailServiceFactory;
		this.emailTemplateSupplier = builder.emailTemplateSupplier;
		this.templateResolver = builder.templateResolver;
	}

	@Override
	public void execute() {
		final EmailAccount emailAccount = emailAccoutSupplier.get();
		final EmailService emailService = emailServiceFactory.create(emailAccount);
		final Template template = emailTemplateSupplier.get();
		final Email email = new Email();
		email.setToAddresses(templateResolver.resolve(template.getTo()));
		email.setCcAddresses(templateResolver.resolve(template.getCc()));
		email.setBccAddresses(templateResolver.resolve(template.getBcc()));
		email.setSubject(templateResolver.resolve(template.getSubject()));
		email.setContent(templateResolver.resolve(template.getBody()));
		emailService.send(email);
	}

}
