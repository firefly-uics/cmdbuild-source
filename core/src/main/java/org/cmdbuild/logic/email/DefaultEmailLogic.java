package org.cmdbuild.logic.email;

import static com.google.common.base.Splitter.on;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.reflect.Reflection.newProxy;
import static java.util.Arrays.asList;
import static org.cmdbuild.common.utils.Reflection.unsupported;
import static org.cmdbuild.data.store.email.EmailConstants.ADDRESSES_SEPARATOR;
import static org.cmdbuild.data.store.email.EmailStatus.OUTGOING;
import static org.cmdbuild.data.store.email.Groupables.reference;
import static org.cmdbuild.data.store.email.Groupables.status;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;

import java.util.UUID;

import javax.activation.DataHandler;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.data.store.email.EmailStatusConverter;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.ForwardingAttachment;
import org.cmdbuild.services.email.ForwardingEmailService;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;

public class DefaultEmailLogic implements EmailLogic {

	private static enum StatusConverter {
		RECEIVED(EmailStatus.RECEIVED) {

			@Override
			public Status status() {
				return received();
			}

		}, //
		DRAFT(EmailStatus.DRAFT) {

			@Override
			public Status status() {
				return draft();
			}

		}, //
		OUTGOING(EmailStatus.OUTGOING) {

			@Override
			public Status status() {
				return outgoing();
			}

		}, //
		SENT(EmailStatus.SENT) {

			@Override
			public Status status() {
				return sent();
			}

		}, //
		UNDEFINED(null) {

			@Override
			public Status status() {
				return null;
			}

		}, //
		;

		private final EmailStatus value;

		private StatusConverter(final EmailStatus value) {
			this.value = value;
		}

		public abstract Status status();

		public EmailStatus value() {
			return value;
		}

		public static StatusConverter of(final EmailStatus value) {
			for (final StatusConverter element : values()) {
				if (element.value.equals(value)) {
					return element;
				}
			}
			return UNDEFINED;
		}

		public static StatusConverter of(final Status status) {
			return new StatusVisitor() {

				private StatusConverter output;

				public StatusConverter convert() {
					if (status != null) {
						status.accept(this);
					} else {
						output = UNDEFINED;
					}
					return output;
				}

				@Override
				public void visit(final Received status) {
					output = RECEIVED;
				}

				@Override
				public void visit(final Draft status) {
					output = DRAFT;
				}

				@Override
				public void visit(final Sent status) {
					output = SENT;
				}

				@Override
				public void visit(final Outgoing status) {
					output = OUTGOING;
				}

			}.convert();
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
			output.setStatus(StatusConverter.of(input.getStatus()).value());
			output.setReference(input.getReference());
			output.setNoSubjectPrefix(input.isNoSubjectPrefix());
			output.setAccount(input.getAccount());
			output.setTemplate(input.getTemplate());
			output.setKeepSynchronization(input.isKeepSynchronization());
			output.setPromptSynchronization(input.isPromptSynchronization());
			output.setDelay(input.getDelay());
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
					.withStatus(StatusConverter.of(input.getStatus()).status()) //
					.withReference(input.getReference()) //
					.withNoSubjectPrefix(input.isNoSubjectPrefix()) //
					.withAccount(input.getAccount()) //
					.withTemplate(input.getTemplate()) //
					.withKeepSynchronization(input.isKeepSynchronization()) //
					.withPromptSynchronization(input.isPromptSynchronization()) //
					.withDelay(input.getDelay()) //
					.build();
		}

	};

	private static final EmailService EMAIL_SERVICE_FOR_INVALID_PROCESS_ID = new ForwardingEmailService() {

		private final EmailService UNSUPPORTED = UnsupportedProxyFactory.of(EmailService.class).create();

		@Override
		protected EmailService delegate() {
			return UNSUPPORTED;
		}

	};

	private static class TemporaryEmail extends ForwardingEmail {

		private final Email delegate;

		public TemporaryEmail(final Email delegate) {
			this.delegate = delegate;
		}

		@Override
		protected Email delegate() {
			return delegate;
		}

	}

	private static final Function<Email, Email> TO_TEMPORARY = new Function<Email, Email>() {

		@Override
		public Email apply(final Email input) {
			return new TemporaryEmail(input);
		}

	};

	private static class EmailAdapter extends org.cmdbuild.services.email.ForwardingEmail {

		private final org.cmdbuild.services.email.Email unsupported = newProxy(org.cmdbuild.services.email.Email.class,
				unsupported("method not supported"));

		private final Email delegate;
		private final EmailAttachmentsLogic emailAttachmentsLogic;

		public EmailAdapter(final Email delegate, final EmailAttachmentsLogic emailAttachmentsLogic) {
			this.delegate = delegate;
			this.emailAttachmentsLogic = emailAttachmentsLogic;
		}

		@Override
		protected org.cmdbuild.services.email.Email delegate() {
			return unsupported;
		}

		@Override
		public String getFromAddress() {
			return delegate.getFromAddress();
		}

		@Override
		public Iterable<String> getToAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(delegate.getToAddresses());
		}

		@Override
		public Iterable<String> getCcAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(delegate.getCcAddresses());
		}

		@Override
		public Iterable<String> getBccAddresses() {
			return on(ADDRESSES_SEPARATOR) //
					.omitEmptyStrings() //
					.trimResults() //
					.split(delegate.getBccAddresses());
		}

		@Override
		public String getSubject() {
			return delegate.getSubject();
		}

		@Override
		public String getContent() {
			return delegate.getContent();
		}

		@Override
		public Iterable<org.cmdbuild.services.email.Attachment> getAttachments() {
			return from(emailAttachmentsLogic.readAll(delegate)) //
					.transform(new Function<Attachment, org.cmdbuild.services.email.Attachment>() {

						@Override
						public org.cmdbuild.services.email.Attachment apply(final Attachment input) {
							final org.cmdbuild.services.email.Attachment output;
							final Optional<DataHandler> dataHandler = emailAttachmentsLogic.read(delegate, input);
							if (dataHandler.isPresent()) {
								final TempDataSource tempDataSource = TempDataSource.newInstance() //
										.withName(input.getFileName()) //
										.build();
								output = new ForwardingAttachment() {

									private final org.cmdbuild.services.email.Attachment unsupported = newProxy(
											org.cmdbuild.services.email.Attachment.class,
											unsupported("method not supported"));

									@Override
									protected org.cmdbuild.services.email.Attachment delegate() {
										return unsupported;
									}

									@Override
									public String getName() {
										return input.getFileName();
									};

									@Override
									public DataHandler getDataHandler() {
										return new DataHandler(tempDataSource);
									};

								};
							} else {
								output = null;
							}
							return output;
						}

					}) //
					.filter(org.cmdbuild.services.email.Attachment.class);
		}

		@Override
		public String getAccount() {
			return delegate.getAccount();
		}

		@Override
		public long getDelay() {
			return delegate.getDelay();
		}

	}

	private final Store<org.cmdbuild.data.store.email.Email> emailStore;
	private final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore;
	private final EmailStatusConverter emailStatusConverter;

	public DefaultEmailLogic( //
			final Store<org.cmdbuild.data.store.email.Email> emailStore, //
			final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore, //
			final EmailStatusConverter emailStatusConverter //
	) {
		this.emailStore = emailStore;
		this.temporaryEmailStore = temporaryEmailStore;
		this.emailStatusConverter = emailStatusConverter;
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

			@Override
			public Status getStatus() {
				/*
				 * newly created e-mails are always drafts
				 */
				return draft();
			}

		});
		final Storable stored = storeOf(email).create(storableEmail);
		final Long id = Long.parseLong(stored.getIdentifier());
		return id;
	}

	@Override
	public Iterable<Email> readAll(final Long reference) {
		return from(concat( //
				from(emailStore.readAll(reference(reference))) //
						.transform(STORE_TO_LOGIC), //
				from(temporaryEmailStore.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(reference, input.getReference());
							}

						}) //
						.transform(STORE_TO_LOGIC) //
						.transform(TO_TEMPORARY) //
		));
	}

	@Override
	public Iterable<Email> readAll(final Status status) {
		return from(concat( //
				from(emailStore.readAll(status(emailStatusConverter.toId(OUTGOING)))) //
						.transform(STORE_TO_LOGIC), //
				from(temporaryEmailStore.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(status, input.getStatus());
							}

						}) //
						.transform(STORE_TO_LOGIC) //
						.transform(TO_TEMPORARY) //
		));
	}

	@Override
	public Email read(final Email email) {
		final org.cmdbuild.data.store.email.Email read = storeOf(email).read(LOGIC_TO_STORE.apply(email));
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
		final Email read = read(email);
		Validate.isTrue( //
				contains(asList(draft(), outgoing()), read.getStatus()), //
				"cannot update e-mail '%s' due to an invalid status", read);
		if (draft().equals(email.getStatus())) {
			Validate.isTrue( //
					contains(asList(draft(), outgoing()), email.getStatus()), //
					"cannot update e-mail due to an invalid new status", email.getStatus());
		} else if (outgoing().equals(email.getStatus())) {
			Validate.isTrue( //
					contains(asList(outgoing()), email.getStatus()), //
					"cannot update e-mail due to an invalid new status", email.getStatus());
		} else {
			Validate.isTrue( //
					contains(asList(draft(), outgoing()), email.getStatus()), //
					"invalid new status", email.getStatus());
		}

		if (draft().equals(read.getStatus())) {
			updateWithNoChecks(email);
		}
	}

	@Override
	public void updateWithNoChecks(final Email email) {
		final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(email);
		storeOf(email).update(storable);
	}

	@Override
	public void delete(final Email email) {
		final Email read = read(email);
		Validate.isTrue( //
				contains(asList(draft()), read.getStatus()), //
				"cannot delete e-mail '%s' due to an invalid status", read);

		final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(read);
		storeOf(read).delete(storable);
	}

	private Store<org.cmdbuild.data.store.email.Email> storeOf(final Email email) {
		return email.isTemporary() ? temporaryEmailStore : emailStore;
	}

}
