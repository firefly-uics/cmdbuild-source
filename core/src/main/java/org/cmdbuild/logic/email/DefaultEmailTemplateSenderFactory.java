package org.cmdbuild.logic.email;

import static com.google.common.base.Splitter.on;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.template.TemplateResolvers.identity;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.joda.time.DateTime.now;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.email.EmailLogic.ForwardingEmail;
import org.cmdbuild.logic.email.EmailLogic.Status;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.services.email.Attachment;
import org.cmdbuild.services.email.Email;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.joda.time.DateTime;

import com.google.common.base.Supplier;

public class DefaultEmailTemplateSenderFactory implements EmailTemplateSenderFactory {

	private static abstract class Builder implements EmailTemplateSenderFactory.Builder {

		protected Supplier<EmailAccount> emailAccoutSupplier;
		protected Supplier<Template> emailTemplateSupplier;
		protected TemplateResolver templateResolver;
		protected Long reference;

		/**
		 * Usable by subclasses only.
		 */
		protected Builder() {
		}

		@Override
		public Builder withEmailAccountSupplier(final Supplier<EmailAccount> emailAccoutSupplier) {
			this.emailAccoutSupplier = emailAccoutSupplier;
			return this;
		}

		@Override
		public Builder withEmailTemplateSupplier(final Supplier<Template> emailTemplateSupplier) {
			this.emailTemplateSupplier = emailTemplateSupplier;
			return this;
		}

		@Override
		public Builder withTemplateResolver(final TemplateResolver templateResolver) {
			this.templateResolver = templateResolver;
			return this;
		}

		@Override
		public Builder withReference(final Long reference) {
			this.reference = reference;
			return this;
		}

		@Override
		public final EmailTemplateSender build() {
			validate();
			return doBuild();
		}

		protected void validate() {
			Validate.notNull(emailAccoutSupplier, "missing '%s' supplier", EmailAccount.class);
			Validate.notNull(emailTemplateSupplier, "missing '%s' supplier", Template.class);
			templateResolver = defaultIfNull(templateResolver, identity());
		}

		protected abstract EmailTemplateSender doBuild();

	}

	private static class DirectEmailTemplateSender implements EmailTemplateSender {

		private static class Builder extends DefaultEmailTemplateSenderFactory.Builder {

			private final EmailServiceFactory emailServiceFactory;

			public Builder(final EmailServiceFactory emailServiceFactory) {
				this.emailServiceFactory = emailServiceFactory;
			}

			@Override
			protected void validate() {
				Validate.notNull(emailServiceFactory, "missing '%s'", EmailServiceFactory.class);
				super.validate();
			}

			@Override
			protected EmailTemplateSender doBuild() {
				return new DirectEmailTemplateSender(this);
			}

		}

		public static Builder newInstance(final EmailServiceFactory emailServiceFactory) {
			return new Builder(emailServiceFactory);
		}

		private final EmailServiceFactory emailServiceFactory;
		private final Supplier<EmailAccount> emailAccoutSupplier;
		private final Supplier<Template> emailTemplateSupplier;
		private final TemplateResolver templateResolver;

		private DirectEmailTemplateSender(final Builder builder) {
			this.emailServiceFactory = builder.emailServiceFactory;
			this.emailAccoutSupplier = builder.emailAccoutSupplier;
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

	private static class TemplateAdapter implements Email {

		private static final Iterable<Attachment> NO_ATTACHMENTS = emptyList();

		private final DateTime OBJECT_CREATION_TIME = now();

		private final Template template;
		private final TemplateResolver templateResolver;

		public TemplateAdapter(final Template template, final TemplateResolver templateResolver) {
			this.template = template;
			this.templateResolver = templateResolver;
		}

		@Override
		public DateTime getDate() {
			return OBJECT_CREATION_TIME;
		}

		@Override
		public String getFromAddress() {
			return templateResolver.resolve(template.getFrom());
		}

		@Override
		public Iterable<String> getToAddresses() {
			return addresses(template.getTo());
		}

		@Override
		public Iterable<String> getCcAddresses() {
			return addresses(template.getCc());
		}

		@Override
		public Iterable<String> getBccAddresses() {
			return addresses(template.getBcc());
		}

		private Iterable<String> addresses(final String value) {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(defaultString(templateResolver.resolve(value)));
		}

		@Override
		public String getSubject() {
			return templateResolver.resolve(template.getSubject());
		}

		@Override
		public String getContent() {
			return templateResolver.resolve(template.getBody());
		}

		@Override
		public Iterable<Attachment> getAttachments() {
			return NO_ATTACHMENTS;
		}

		@Override
		public String getAccount() {
			return template.getAccount();
		}

		@Override
		public long getDelay() {
			return 0;
		}

	}

	private static class QueuedEmailTemplateSender implements EmailTemplateSender {

		private static class Builder extends DefaultEmailTemplateSenderFactory.Builder {

			private final EmailLogic emailLogic;

			public Builder(final EmailLogic emailLogic) {
				this.emailLogic = emailLogic;
			}

			@Override
			protected void validate() {
				Validate.notNull(emailLogic, "missing '%s'", EmailLogic.class);
				super.validate();
			}

			@Override
			protected EmailTemplateSender doBuild() {
				return new QueuedEmailTemplateSender(this);
			}

		}

		public static Builder newInstance(final EmailLogic emailLogic) {
			return new Builder(emailLogic);
		}

		private final EmailLogic emailLogic;
		private final Supplier<EmailAccount> emailAccoutSupplier;
		private final Supplier<Template> emailTemplateSupplier;
		private final TemplateResolver templateResolver;
		private final Long reference;

		private QueuedEmailTemplateSender(final Builder builder) {
			this.emailLogic = builder.emailLogic;
			this.emailAccoutSupplier = builder.emailAccoutSupplier;
			this.emailTemplateSupplier = builder.emailTemplateSupplier;
			this.templateResolver = builder.templateResolver;
			this.reference = builder.reference;
		}

		@Override
		public void execute() {
			final EmailLogic.Email email = new OutgoingEmail(emailTemplateSupplier.get(), reference,
					emailAccoutSupplier.get(), templateResolver);
			final Long id = emailLogic.create(email);
			emailLogic.update(new ForwardingEmail() {

				@Override
				protected EmailLogic.Email delegate() {
					return email;
				}

				@Override
				public Long getId() {
					return id;
				}

			});
		}
	}

	private static class OutgoingEmail implements EmailLogic.Email {

		private final DateTime OBJECT_CREATION_TIME = now();

		private final Template template;
		private final Long reference;
		private final EmailAccount emailAccount;
		private final TemplateResolver templateResolver;

		public OutgoingEmail(final Template template, final Long reference, final EmailAccount emailAccount,
				final TemplateResolver templateResolver) {
			this.template = template;
			this.reference = reference;
			this.emailAccount = emailAccount;
			this.templateResolver = templateResolver;
		}

		@Override
		public Long getId() {
			return null;
		}

		@Override
		public DateTime getDate() {
			return OBJECT_CREATION_TIME;
		}

		@Override
		public String getFromAddress() {
			return templateResolver.resolve(template.getFrom());
		}

		@Override
		public String getToAddresses() {
			return templateResolver.resolve(template.getTo());
		}

		@Override
		public String getCcAddresses() {
			return templateResolver.resolve(template.getCc());
		}

		@Override
		public String getBccAddresses() {
			return templateResolver.resolve(template.getBcc());
		}

		@Override
		public String getSubject() {
			return templateResolver.resolve(template.getSubject());
		}

		@Override
		public String getContent() {
			return templateResolver.resolve(template.getBody());
		}

		@Override
		public String getAccount() {
			return defaultString(template.getAccount(), emailAccount.getName());
		}

		@Override
		public long getDelay() {
			return 0;
		}

		@Override
		public Status getStatus() {
			return outgoing();
		}

		@Override
		public Long getReference() {
			return reference;
		}

		@Override
		public String getNotifyWith() {
			return null;
		}

		@Override
		public boolean isNoSubjectPrefix() {
			return true;
		}

		@Override
		public boolean isTemporary() {
			return false;
		}

		@Override
		public String getTemplate() {
			return null;
		}

		@Override
		public boolean isKeepSynchronization() {
			return false;
		}

		@Override
		public boolean isPromptSynchronization() {
			return false;
		}

	}

	private final EmailServiceFactory emailServiceFactory;
	private final EmailLogic emailLogic;

	public DefaultEmailTemplateSenderFactory(final EmailServiceFactory emailServiceFactory, final EmailLogic emailLogic) {
		this.emailServiceFactory = emailServiceFactory;
		this.emailLogic = emailLogic;
	}

	@Override
	public Builder direct() {
		return DirectEmailTemplateSender.newInstance(emailServiceFactory);
	}

	@Override
	public Builder queued() {
		return QueuedEmailTemplateSender.newInstance(emailLogic);
	}

}
