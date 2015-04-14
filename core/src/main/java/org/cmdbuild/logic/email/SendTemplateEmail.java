package org.cmdbuild.logic.email;

import static com.google.common.base.Splitter.on;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.common.template.TemplateResolvers.identity;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.joda.time.DateTime.now;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.services.email.Attachment;
import org.cmdbuild.services.email.Email;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.ForwardingEmail;
import org.joda.time.DateTime;

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

	private static class TemplateAdapter extends ForwardingEmail {

		private static final Email unsupported = newProxy(Email.class, unsupported("method not supported"));

		private static final Iterable<Attachment> NO_ATTACHMENTS = emptyList();

		private final DateTime OBJECT_CREATION_TIME = now();

		private final Template delegate;
		private final TemplateResolver templateResolver;

		public TemplateAdapter(final Template delegate, final TemplateResolver templateResolver) {
			this.delegate = delegate;
			this.templateResolver = templateResolver;
		}

		@Override
		protected Email delegate() {
			return unsupported;
		}

		@Override
		public DateTime getDate() {
			return OBJECT_CREATION_TIME;
		}

		@Override
		public String getFromAddress() {
			return templateResolver.resolve(delegate.getFrom());
		}

		@Override
		public Iterable<String> getToAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(templateResolver.resolve(delegate.getTo()));
		}

		@Override
		public Iterable<String> getCcAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(templateResolver.resolve(delegate.getCc()));
		}

		@Override
		public Iterable<String> getBccAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(templateResolver.resolve(delegate.getBcc()));
		}

		@Override
		public String getSubject() {
			return templateResolver.resolve(delegate.getSubject());
		}

		@Override
		public String getContent() {
			return templateResolver.resolve(delegate.getBody());
		}

		@Override
		public Iterable<Attachment> getAttachments() {
			return NO_ATTACHMENTS;
		}

		@Override
		public String getAccount() {
			return delegate.getAccount();
		}

		@Override
		public long getDelay() {
			return 0;
		}

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
		final EmailService emailService = emailServiceFactory.create(emailAccoutSupplier);
		final Template template = emailTemplateSupplier.get();
		emailService.send(new TemplateAdapter(template, templateResolver));
	}

}
