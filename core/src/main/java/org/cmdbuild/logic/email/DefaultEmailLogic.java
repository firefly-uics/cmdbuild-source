package org.cmdbuild.logic.email;

import static com.google.common.base.Suppliers.memoize;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.contains;
import static com.google.common.collect.Maps.newHashMap;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.cmdbuild.common.utils.guava.Suppliers.firstNotNull;
import static org.cmdbuild.common.utils.guava.Suppliers.nullOnException;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.draft;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.outgoing;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.received;
import static org.cmdbuild.logic.email.EmailLogic.Statuses.sent;
import static org.cmdbuild.services.email.Predicates.isDefault;
import static org.cmdbuild.services.email.Predicates.named;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.utils.TempDataSource;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreSupplier;
import org.cmdbuild.data.store.email.EmailOwnerGroupable;
import org.cmdbuild.data.store.email.EmailStatus;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.logic.email.EmailAttachmentsLogic.Attachment;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.ForwardingEmailService;
import org.cmdbuild.services.email.SubjectHandler;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

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

	private final Store<org.cmdbuild.data.store.email.Email> emailStore;
	private final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore;
	private final EmailServiceFactory emailServiceFactory;
	private final Store<EmailAccount> emailAccountStore;
	private final SubjectHandler subjectHandler;
	private final Notifier notifier;
	private final EmailAttachmentsLogic emailAttachmentsLogic;

	public DefaultEmailLogic( //
			final Store<org.cmdbuild.data.store.email.Email> emailStore, //
			final Store<org.cmdbuild.data.store.email.Email> temporaryEmailStore, //
			final EmailServiceFactory emailServiceFactory, //
			final Store<EmailAccount> emailAccountStore, //
			final SubjectHandler subjectHandler, //
			final Notifier notifier, //
			final EmailAttachmentsLogic emailAttachmentsLogic //
	) {
		this.emailStore = emailStore;
		this.temporaryEmailStore = temporaryEmailStore;
		this.emailServiceFactory = emailServiceFactory;
		this.emailAccountStore = emailAccountStore;
		this.subjectHandler = subjectHandler;
		this.notifier = notifier;
		this.emailAttachmentsLogic = emailAttachmentsLogic;
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
				from(emailStore.readAll(EmailOwnerGroupable.of(reference))) //
						.transform(STORE_TO_LOGIC), //
				from(temporaryEmailStore.readAll()) //
						.filter(new Predicate<org.cmdbuild.data.store.email.Email>() {

							@Override
							public boolean apply(final org.cmdbuild.data.store.email.Email input) {
								return ObjectUtils.equals(reference, input.getReference());
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
			final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(email);
			storeOf(email).update(storable);
		}

		if (outgoing().equals(email.getStatus())) {
			send(read(email));
		}
	}

	private void send(final Email email) {
		try {
			final Supplier<EmailAccount> accountSupplier = accountSupplierOf(email);
			final Email _email = new ForwardingEmail() {

				@Override
				protected Email delegate() {
					return email;
				}

				@Override
				public String getFromAddress() {
					return defaultIfBlank(super.getFromAddress(), accountSupplier.get().getAddress());
				}

				@Override
				public Status getStatus() {
					return sent();
				}

			};
			send0(accountSupplier, _email);
			storeOf(email).update(LOGIC_TO_STORE.apply(_email));
		} catch (final CMDBException e) {
			notifier.warn(e);
		} catch (final Throwable e) {
			notifier.warn(CMDBWorkflowException.WorkflowExceptionType.WF_EMAIL_NOT_SENT.createException());
		}
	}

	private Supplier<EmailAccount> accountSupplierOf(final Email email) {
		final Supplier<EmailAccount> defaultAccountSupplier = memoize(nullOnException(defaultAccountSupplier()));
		final Supplier<EmailAccount> emailAccountSupplier = nullOnException(StoreSupplier.of(EmailAccount.class,
				emailAccountStore, named(email.getAccount())));
		final Supplier<EmailAccount> accountSupplier = memoize(firstNotNull(asList(emailAccountSupplier,
				defaultAccountSupplier)));
		return accountSupplier;
	}

	private void send0(final Supplier<EmailAccount> accountSupplier, final Email email) throws IOException {
		/*
		 * we don't want to save the altered subject, so we are keeping it in a
		 * different copy of the e-mail
		 */
		final Email _email = new ForwardingEmail() {

			@Override
			protected Email delegate() {
				return email;
			}

			@Override
			public String getSubject() {
				final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(delegate());
				final String subject;
				if (subjectHandler.parse(storable.getSubject()).hasExpectedFormat()) {
					subject = storable.getSubject();
				} else {
					subject = defaultIfBlank(subjectHandler.compile(storable).getSubject(), EMPTY);
				}
				return subject;
			}

		};
		final org.cmdbuild.data.store.email.Email storable = LOGIC_TO_STORE.apply(_email);
		emailService(storable.getReference(), accountSupplier).send(storable, attachmentsOf(_email));
	}

	private Map<URL, String> attachmentsOf(final Email read) throws IOException {
		final Map<URL, String> attachments = newHashMap();
		for (final Attachment attachment : emailAttachmentsLogic.readAll(read)) {
			final Optional<DataHandler> dataHandler = emailAttachmentsLogic.read(read, attachment);
			if (dataHandler.isPresent()) {
				final TempDataSource tempDataSource = TempDataSource.newInstance() //
						.withName(attachment.getFileName()) //
						.build();
				copy(dataHandler.get(), tempDataSource);
				final URL url = tempDataSource.getFile().toURI().toURL();
				attachments.put(url, attachment.getFileName());
			}
		}
		return attachments;
	}

	private void copy(final DataHandler from, final DataSource to) throws IOException {
		final InputStream inputStream = from.getInputStream();
		final OutputStream outputStream = to.getOutputStream();
		IOUtils.copy(inputStream, outputStream);
		IOUtils.closeQuietly(inputStream);
		IOUtils.closeQuietly(outputStream);
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

	private StoreSupplier<EmailAccount> defaultAccountSupplier() {
		return StoreSupplier.of(EmailAccount.class, emailAccountStore, isDefault());
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
