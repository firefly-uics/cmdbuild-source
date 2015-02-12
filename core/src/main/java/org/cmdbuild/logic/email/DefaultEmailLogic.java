package org.cmdbuild.logic.email;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;
import static org.cmdbuild.services.email.Predicates.isDefault;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreSupplier;
import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.data.store.email.ExtendedEmailTemplate;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.ForwardingEmailService;
import org.cmdbuild.services.email.SubjectHandler;
import org.joda.time.DateTime;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ForwardingObject;

public class DefaultEmailLogic implements EmailLogic {

	public static abstract class ForwardingEmail extends ForwardingObject implements Email {

		@Override
		protected abstract Email delegate();

		@Override
		public Long getId() {
			return delegate().getId();
		}

		@Override
		public String getFromAddress() {
			return delegate().getFromAddress();
		}

		@Override
		public String getToAddresses() {
			return delegate().getToAddresses();
		}

		@Override
		public String getCcAddresses() {
			return delegate().getCcAddresses();
		}

		@Override
		public String getBccAddresses() {
			return delegate().getBccAddresses();
		}

		@Override
		public String getSubject() {
			return delegate().getSubject();
		}

		@Override
		public String getContent() {
			return delegate().getContent();
		}

		@Override
		public DateTime getDate() {
			return delegate().getDate();
		}

		@Override
		public EmailStatus getStatus() {
			return delegate().getStatus();
		}

		@Override
		public Long getActivityId() {
			return delegate().getActivityId();
		}

		@Override
		public String getNotifyWith() {
			return delegate().getNotifyWith();
		}

		@Override
		public boolean isNoSubjectPrefix() {
			return delegate().isNoSubjectPrefix();
		}

		@Override
		public String getAccount() {
			return delegate().getAccount();
		}

		@Override
		public boolean isTemporary() {
			return delegate().isTemporary();
		}

		@Override
		public String getTemplate() {
			return delegate().getTemplate();
		}

	}

	public static class EmailImpl implements Email {

		public static class Builder implements org.apache.commons.lang3.builder.Builder<Email> {

			private Long id;
			private String fromAddress;
			private String toAddresses;
			private String ccAddresses;
			private String bccAddresses;
			private String subject;
			private String content;
			private String notifyWith;
			private DateTime date;
			private EmailStatus status;
			private Long activityId;
			private boolean noSubjectPrefix;
			private String account;
			private boolean temporary;
			private String template;

			private Builder() {
				// use factory method
			}

			@Override
			public Email build() {
				return new EmailImpl(this);
			}

			public Builder withId(final Long id) {
				this.id = id;
				return this;
			}

			public Builder withFromAddress(final String fromAddress) {
				this.fromAddress = fromAddress;
				return this;
			}

			public Builder withToAddresses(final String toAddresses) {
				this.toAddresses = toAddresses;
				return this;
			}

			public Builder withCcAddresses(final String ccAddresses) {
				this.ccAddresses = ccAddresses;
				return this;
			}

			public Builder withBccAddresses(final String bccAddresses) {
				this.bccAddresses = bccAddresses;
				return this;
			}

			public Builder withSubject(final String subject) {
				this.subject = subject;
				return this;
			}

			public Builder withContent(final String content) {
				this.content = content;
				return this;
			}

			public Builder withNotifyWith(final String notifyWith) {
				this.notifyWith = notifyWith;
				return this;
			}

			public Builder withDate(final DateTime date) {
				this.date = date;
				return this;
			}

			public Builder withStatus(final EmailStatus status) {
				this.status = status;
				return this;
			}

			public Builder withActivityId(final Long activityId) {
				this.activityId = activityId;
				return this;
			}

			public Builder withNoSubjectPrefix(final boolean noSubjectPrefix) {
				this.noSubjectPrefix = noSubjectPrefix;
				return this;
			}

			public Builder withAccount(final String account) {
				this.account = account;
				return this;
			}

			public Builder withTemporary(final boolean temporary) {
				this.temporary = temporary;
				return this;
			}

			public Builder withTemplate(final String template) {
				this.template = template;
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final Long id;
		private final String fromAddress;
		private final String toAddresses;
		private final String ccAddresses;
		private final String bccAddresses;
		private final String subject;
		private final String content;
		private final String notifyWith;
		private final DateTime date;
		private final EmailStatus status;
		private final Long activityId;
		private final boolean noSubjectPrefix;
		private final String account;
		private final boolean temporary;
		private final String template;

		private EmailImpl(final Builder builder) {
			this.id = builder.id;
			this.fromAddress = builder.fromAddress;
			this.toAddresses = builder.toAddresses;
			this.ccAddresses = builder.ccAddresses;
			this.bccAddresses = builder.bccAddresses;
			this.subject = builder.subject;
			this.content = builder.content;
			this.notifyWith = builder.notifyWith;
			this.date = builder.date;
			this.status = builder.status;
			this.activityId = builder.activityId;
			this.noSubjectPrefix = builder.noSubjectPrefix;
			this.account = builder.account;
			this.temporary = builder.temporary;
			this.template = builder.template;
		}

		@Override
		public Long getId() {
			return id;
		}

		@Override
		public String getFromAddress() {
			return fromAddress;
		}

		@Override
		public String getToAddresses() {
			return toAddresses;
		}

		@Override
		public String getCcAddresses() {
			return ccAddresses;
		}

		@Override
		public String getBccAddresses() {
			return bccAddresses;
		}

		@Override
		public String getSubject() {
			return subject;
		}

		@Override
		public String getContent() {
			return content;
		}

		@Override
		public DateTime getDate() {
			return date;
		}

		@Override
		public EmailStatus getStatus() {
			return status;
		}

		@Override
		public Long getActivityId() {
			return activityId;
		}

		@Override
		public String getNotifyWith() {
			return notifyWith;
		}

		@Override
		public boolean isNoSubjectPrefix() {
			return noSubjectPrefix;
		}

		@Override
		public String getAccount() {
			return account;
		}

		@Override
		public boolean isTemporary() {
			return temporary;
		}

		@Override
		public String getTemplate() {
			return template;
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj) {
				return true;
			}

			if (!(obj instanceof Email)) {
				return false;
			}

			final Email other = Email.class.cast(obj);
			return new EqualsBuilder() //
					.append(this.getId(), other.getId()) //
					.append(this.getFromAddress(), other.getFromAddress()) //
					.append(this.getToAddresses(), other.getToAddresses()) //
					.append(this.getCcAddresses(), other.getCcAddresses()) //
					.append(this.getBccAddresses(), other.getBccAddresses()) //
					.append(this.getSubject(), other.getSubject()) //
					.append(this.getContent(), other.getContent()) //
					.append(this.getDate(), other.getDate()) //
					.append(this.getStatus(), other.getStatus()) //
					.append(this.getActivityId(), other.getActivityId()) //
					.append(this.getNotifyWith(), other.getNotifyWith()) //
					.append(this.isNoSubjectPrefix(), other.isNoSubjectPrefix()) //
					.append(this.getAccount(), other.getAccount()) //
					.append(this.isTemporary(), other.isTemporary()) //
					.append(this.getTemplate(), other.getTemplate()) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(id) //
					.append(fromAddress) //
					.append(toAddresses) //
					.append(ccAddresses) //
					.append(bccAddresses) //
					.append(subject) //
					.append(content) //
					.append(date) //
					.append(status) //
					.append(activityId) //
					.append(notifyWith) //
					.append(noSubjectPrefix) //
					.append(account) //
					.append(temporary) //
					.append(template) //
					.toHashCode();
		}

		@Override
		public final String toString() {
			return ToStringBuilder.reflectionToString(this, SHORT_PREFIX_STYLE).toString();
		}

	}

	private static final Function<Email, org.cmdbuild.data.store.email.Email> LOGIC_TO_STORE = new Function<Email, org.cmdbuild.data.store.email.Email>() {

		@Override
		public org.cmdbuild.data.store.email.Email apply(final Email input) {
			final org.cmdbuild.data.store.email.Email output = new org.cmdbuild.data.store.email.Email(input.getId());
			output.setFromAddress(input.getFromAddress());
			output.setToAddresses(input.getToAddresses());
			output.setCcAddresses(input.getCcAddresses());
			output.setBccAddresses(input.getBccAddresses());
			output.setSubject(input.getSubject());
			output.setContent(input.getContent());
			output.setNotifyWith(input.getNotifyWith());
			output.setDate(input.getDate());
			output.setStatus(input.getStatus());
			output.setActivityId(input.getActivityId());
			output.setNoSubjectPrefix(input.isNoSubjectPrefix());
			output.setAccount(input.getAccount());
			output.setTemplate(input.getTemplate());
			return output;
		}

	};

	private static final Function<org.cmdbuild.data.store.email.Email, Email> STORE_TO_LOGIC = new Function<org.cmdbuild.data.store.email.Email, Email>() {

		@Override
		public Email apply(final org.cmdbuild.data.store.email.Email input) {
			return EmailImpl.newInstance() //
					.withId(input.getId()) //
					.withFromAddress(input.getFromAddress()) //
					.withToAddresses(input.getToAddresses()) //
					.withCcAddresses(input.getCcAddresses()) //
					.withBccAddresses(input.getBccAddresses()) //
					.withSubject(input.getSubject()) //
					.withContent(input.getContent()) //
					.withNotifyWith(input.getNotifyWith()) //
					.withDate(input.getDate()) //
					.withStatus(input.getStatus()) //
					.withActivityId(input.getActivityId()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemplate(input.getTemplate()) //
					.build();
		}

	};

	private static final Function<org.cmdbuild.data.store.email.Email, Long> EMAIL_ID = new Function<org.cmdbuild.data.store.email.Email, Long>() {

		@Override
		public Long apply(final org.cmdbuild.data.store.email.Email input) {
			return input.getId();
		}

	};

	private static final EmailService EMAIL_SERVICE_FOR_INVALID_PROCESS_ID = new ForwardingEmailService() {

		private final EmailService UNSUPPORTED = UnsupportedProxyFactory.of(EmailService.class).create();
		private final Iterable<org.cmdbuild.data.store.email.Email> NO_EMAILS = emptyList();
		private final Iterable<ExtendedEmailTemplate> NO_EMAIL_TEMPLATES = emptyList();

		@Override
		protected EmailService delegate() {
			return UNSUPPORTED;
		}

		@Override
		public Iterable<org.cmdbuild.data.store.email.Email> getEmails(final Long processId) {
			return NO_EMAILS;
		};

		@Override
		public org.cmdbuild.data.store.email.Email getEmail(final Long id) {
			return null;
		}

		@Override
		public Iterable<org.cmdbuild.data.store.email.Email> getOutgoingEmails(final Long processId) {
			return NO_EMAILS;
		};

		@Override
		public Iterable<ExtendedEmailTemplate> getEmailTemplates(final org.cmdbuild.data.store.email.Email email) {
			return NO_EMAIL_TEMPLATES;
		}

	};

	private static final EmailStatus MISSING_STATUS = null;
	private static final Collection<EmailStatus> SAVEABLE_STATUSES = asList(EmailStatus.DRAFT, MISSING_STATUS);

	private final EmailServiceFactory emailServiceFactory;
	private final Store<EmailAccount> emailAccountStore;
	private final SubjectHandler subjectHandler;
	private final Notifier notifier;
	private final Store<org.cmdbuild.data.store.email.Email> store;

	public DefaultEmailLogic( //
			final EmailServiceFactory emailServiceFactory, //
			final Store<EmailAccount> emailAccountStore, //
			final SubjectHandler subjectHandler, //
			final Notifier notifier, //
			final Store<org.cmdbuild.data.store.email.Email> store //
	) {
		this.emailServiceFactory = emailServiceFactory;
		this.emailAccountStore = emailAccountStore;
		this.subjectHandler = subjectHandler;
		this.notifier = notifier;
		this.store = store;
	}

	@Override
	public Long create(final Email email) {
		final org.cmdbuild.data.store.email.Email storableEmail = LOGIC_TO_STORE.apply(new ForwardingEmail() {

			@Override
			protected Email delegate() {
				return email;
			}

			@Override
			public Long getId() {
				return isTemporary() ? generateId() : super.getId();
			}

			private int generateId() {
				return UUID.randomUUID().hashCode();
			}

		});
		final Long id;
		if (email.isTemporary()) {
			final Storable stored = store.create(storableEmail);
			id = Long.parseLong(stored.getIdentifier());
		} else {
			id = emailService().save(storableEmail);
		}
		return id;
	}

	@Override
	public Email read(final Email email) {
		final org.cmdbuild.data.store.email.Email read;
		if (email.isTemporary()) {
			read = store.read(LOGIC_TO_STORE.apply(email));
		} else {
			read = emailService().getEmail(email.getId());
		}
		return new ForwardingEmail() {

			@Override
			protected Email delegate() {
				return STORE_TO_LOGIC.apply(read);
			}

			@Override
			public boolean isTemporary() {
				return email.isTemporary();
			}

		};
	}

	@Override
	public void update(final Email email) {
		final org.cmdbuild.data.store.email.Email storableEmail = LOGIC_TO_STORE.apply(email);
		if (email.isTemporary()) {
			store.update(storableEmail);
		} else {
			emailService().save(storableEmail);
		}
	}

	@Override
	public void delete(final Email email) {
		if (email.isTemporary()) {
			store.delete(LOGIC_TO_STORE.apply(email));
		} else {
			emailService().delete(LOGIC_TO_STORE.apply(email));
		}
	}

	@Override
	public Iterable<Email> getEmails(final Long processCardId) {
		return from(concat( //
				from(emailService(processCardId).getEmails(processCardId)) //
						.transform(STORE_TO_LOGIC), //
				from(store.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(processCardId, input.getActivityId());
							}

						}) //
						.transform(STORE_TO_LOGIC) //
						.transform(new Function<Email, Email>() {

							@Override
							public Email apply(final Email input) {
								return new ForwardingEmail() {

									@Override
									protected Email delegate() {
										return input;
									}

									@Override
									public boolean isTemporary() {
										return true;
									}

								};
							}

						})) //
		);
	}

	@Override
	public void sendOutgoingAndDraftEmails(final Long processCardId) {
		// final Supplier<EmailAccount> defaultEmailAccountSupplier =
		// memoize(nullOnException(defaultEmailAccountSupplier()));
		// final EmailService defaultEmailService = emailService(processCardId);
		// for (final Email email :
		// defaultEmailService.getOutgoingEmails(processCardId)) {
		// try {
		// final Supplier<EmailAccount> specificEmailAccountSupplier =
		// nullOnException(StoreSupplier.of(
		// EmailAccount.class, emailAccountStore, named(email.getAccount())));
		// final Supplier<EmailAccount> emailAccountSupplier =
		// memoize(firstNotNull(asList(
		// specificEmailAccountSupplier, defaultEmailAccountSupplier)));
		// if (isEmpty(email.getFromAddress())) {
		// email.withFromAddress(emailAccountSupplier.get().getAddress());
		// }
		// if (!subjectHandler.parse(email.getSubject()).hasExpectedFormat()) {
		// email.withSubject(defaultIfBlank(subjectHandler.compile(email).getSubject(),
		// EMPTY));
		// }
		// emailService(processCardId, emailAccountSupplier).send(email,
		// attachmentsOf(email));
		// email.withStatus(EmailStatus.SENT);
		// } catch (final CMDBException e) {
		// notifier.warn(e);
		// email.withStatus(EmailStatus.OUTGOING);
		// } catch (final Throwable e) {
		// notifier.warn(CMDBWorkflowException.WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException());
		// email.withStatus(EmailStatus.OUTGOING);
		// }
		// defaultEmailService.save(email);
		// }
	}

	/**
	 * Deletes all {@link Email}s with the specified id and for the specified
	 * process' id. Only draft mails can be deleted.
	 */
	@Override
	public void deleteEmails(final Long processCardId, final Iterable<Long> emailIds) {
		if (isEmpty(emailIds)) {
			return;
		}
		final Map<Long, org.cmdbuild.data.store.email.Email> storedEmails = uniqueIndex(emailService(processCardId)
				.getEmails(processCardId), EMAIL_ID);
		for (final Long emailId : emailIds) {
			final org.cmdbuild.data.store.email.Email found = storedEmails.get(emailId);
			Validate.notNull(found, "email not found");
			Validate.isTrue(SAVEABLE_STATUSES.contains(found.getStatus()), "specified email have no compatible status");
			emailService(processCardId).delete(found);
		}
	}

	/**
	 * Saves all specified {@link EmailSubmission}s for the specified process'
	 * id. Only draft mails can be saved, others are skipped.
	 */
	@Override
	public void saveEmails(final Long processCardId, final Iterable<? extends EmailSubmission> emails) {
		// if (isEmpty(emails)) {
		// return;
		// }
		// final Map<Long, Email> storedEmails =
		// uniqueIndex(emailService(processCardId).getEmails(processCardId),
		// EMAIL_ID);
		// for (final EmailSubmission emailSubmission : emails) {
		// final Email alreadyStoredEmailSubmission =
		// storedEmails.get(emailSubmission.getId());
		// final boolean alreadyStored = (alreadyStoredEmailSubmission != null);
		// final Email maybeUpdateable = alreadyStored ?
		// alreadyStoredEmailSubmission : emailSubmission;
		// if (SAVEABLE_STATUSES.contains(maybeUpdateable.getStatus())) {
		// maybeUpdateable.withActivityId(processCardId);
		// final Long savedId =
		// emailService(processCardId).save(maybeUpdateable);
		// if (!alreadyStored) {
		// moveAttachmentsFromTemporaryToFinalPosition(emailSubmission.getTemporaryId(),
		// savedId.toString());
		// }
		// }
		//
		// }
	}

	private EmailService emailService() {
		return emailServiceFactory.create();
	}

	private EmailService emailService(final Long processCardId) {
		return emailService(processCardId, defaultEmailAccountSupplier());
	}

	private EmailService emailService(final Long processCardId, final Supplier<EmailAccount> emailAccountSupplier) {
		final boolean isValid = (processCardId != null) && (processCardId > 0);
		if (!isValid) {
			logger.warn("invalid process id, returning a safe email service");
		}
		final EmailService emailService;
		if (isValid) {
			emailService = emailServiceFactory.create(emailAccountSupplier);
		} else {
			emailService = EMAIL_SERVICE_FOR_INVALID_PROCESS_ID;
		}
		return emailService;
	}

	private StoreSupplier<EmailAccount> defaultEmailAccountSupplier() {
		return StoreSupplier.of(EmailAccount.class, emailAccountStore, isDefault());
	}

}
