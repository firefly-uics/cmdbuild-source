package org.cmdbuild.logic.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.TemplateResolverImpl;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailTemplate;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.StoreDocument;
import org.cmdbuild.logic.dms.StoreDocument.Document;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.StartProcess.Hook;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.email.Attachment;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.model.email.EmailConstants;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.email.CollectingEmailCallbackHandler;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailAccountConfiguration;
import org.cmdbuild.services.email.EmailPersistence;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.services.email.SubjectHandler.ParsedSubject;
import org.cmdbuild.services.scheduler.Command;
import org.cmdbuild.services.scheduler.DefaultJob;
import org.cmdbuild.services.scheduler.SafeCommand;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

public class ReadEmailTaskJobFactory extends AbstractJobFactory<ReadEmailTask> {

	private static interface Action {

		void execute(Email email);

	}

	private static class ReadEmail implements Command {

		private static final Logger logger = Log.EMAIL;
		private static Marker marker = MarkerFactory.getMarker(ReadEmail.class.getName());

		public static class Builder implements org.apache.commons.lang3.builder.Builder<ReadEmail> {

			private EmailService emailService;
			private Predicate<Email> predicate;
			private final Collection<Triple<Predicate<Email>, Function<Email, Email>, Action>> triples = Lists
					.newArrayList();

			private Builder() {
				// use factory method
			}

			@Override
			public ReadEmail build() {
				validate();
				return new ReadEmail(this);
			}

			private void validate() {
				Validate.notNull(emailService, "invalid email service");
				Validate.notNull(predicate, "invalid predicate");
			}

			public Builder withEmailService(final EmailService emailService) {
				this.emailService = emailService;
				return this;
			}

			public Builder withPredicate(final Predicate<Email> predicate) {
				this.predicate = predicate;
				return this;
			}

			public Builder withAction(final Predicate<Email> predicate, final Function<Email, Email> function,
					final Action action) {
				this.triples.add(ImmutableTriple.of(predicate, function, action));
				return this;
			}

		}

		public static Builder newInstance() {
			return new Builder();
		}

		private final EmailService service;
		private final Predicate<Email> predicate;
		private final Iterable<Triple<Predicate<Email>, Function<Email, Email>, Action>> triples;

		private ReadEmail(final Builder builder) {
			this.service = builder.emailService;
			this.predicate = builder.predicate;
			this.triples = builder.triples;
		}

		@Override
		public void execute() {
			logger.info(marker, "starting synchronization job");
			final CollectingEmailCallbackHandler callbackHandler = CollectingEmailCallbackHandler.newInstance() //
					.withPredicate(predicate) //
					.build();
			service.receive(callbackHandler);

			logger.info(marker, "executing actions");
			for (final Email email : callbackHandler.getEmails()) {
				for (final Triple<Predicate<Email>, Function<Email, Email>, Action> triple : triples) {
					if (triple.getLeft().apply(email)) {
						final Email adapted = triple.getMiddle().apply(email);
						service.save(adapted);
						triple.getRight().execute(adapted);
					}
				}
			}
			logger.info(marker, "finishing synchronization job");
		}

	}

	private final Store<EmailAccount> emailAccountStore;
	private final ConfigurableEmailServiceFactory emailServiceFactory;
	private final SubjectHandler subjectHandler;
	private final EmailPersistence emailPersistence;
	private final WorkflowLogic workflowLogic;
	private final DmsLogic dmsLogic;

	public ReadEmailTaskJobFactory( //
			final Store<EmailAccount> emailAccountStore, //
			final ConfigurableEmailServiceFactory emailServiceFactory, //
			final SubjectHandler subjectHandler, //
			final EmailPersistence emailPersistence, //
			final WorkflowLogic workflowLogic, //
			final DmsLogic dmsLogic) {
		this.emailAccountStore = emailAccountStore;
		this.emailServiceFactory = emailServiceFactory;
		this.subjectHandler = subjectHandler;
		this.emailPersistence = emailPersistence;
		this.workflowLogic = workflowLogic;
		this.dmsLogic = dmsLogic;
	}

	@Override
	protected Class<ReadEmailTask> getType() {
		return ReadEmailTask.class;
	}

	private static final Predicate<Email> ALWAYS = Predicates.alwaysTrue();

	private static final Predicate<ReadEmailTask> SEND_NOTIFICATION = new Predicate<ReadEmailTask>() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isNotificationRuleActive();
		}

	};

	private static final Predicate<ReadEmailTask> STORE_ATTACHMENTS = new Predicate<ReadEmailTask>() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isAttachmentsRuleActive();
		}

	};

	private static final Predicate<ReadEmailTask> START_PROCESS = new Predicate<ReadEmailTask>() {

		@Override
		public boolean apply(final ReadEmailTask input) {
			return input.isWorkflowRuleActive();
		}

	};

	private static final Predicate<Email> HAS_ATTACHMENTS = new Predicate<Email>() {

		@Override
		public boolean apply(final Email email) {
			return !isEmpty(email.getAttachments());
		}

	};

	private final Predicate<Email> SUBJECT_MATCHES = new Predicate<Email>() {

		@Override
		public boolean apply(final Email email) {
			final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
			if (!parsedSubject.hasExpectedFormat()) {
				return false;
			}

			try {
				emailPersistence.getEmail(parsedSubject.getEmailId());
			} catch (final Exception e) {
				return false;
			}

			return true;
		}

	};

	private static final Function<Email, Email> NO_ADAPTATIONS = new Function<Email, Email>() {

		@Override
		public Email apply(final Email email) {
			return email;
		}

	};

	private final Function<Email, Email> STRIP_SUBJECT_AND_SET_PARENT_DATA = new Function<Email, Email>() {

		@Override
		public Email apply(final Email email) {
			final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
			Validate.isTrue(parsedSubject.hasExpectedFormat(), "invalid subject format");
			final Email parentEmail = emailPersistence.getEmail(parsedSubject.getEmailId());
			email.setSubject(parsedSubject.getRealSubject());
			email.setActivityId(parentEmail.getActivityId());
			email.setNotifyWith(parentEmail.getNotifyWith());
			return email;
		}

	};

	@Override
	protected Job doCreate(final ReadEmailTask task) {
		final String emailAccountName = task.getEmailAccount();
		final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName);
		final EmailConfiguration emailConfiguration = emailConfigurationFrom(selectedEmailAccount);
		final EmailService service = emailServiceFactory.create(emailConfiguration);

		final ReadEmail.Builder readEmail = ReadEmail.newInstance() //
				.withEmailService(service) //
				.withPredicate(predicate(task));

		if (SEND_NOTIFICATION.apply(task)) {
			logger.info(marker, "adding notification action");
			readEmail.withAction(SUBJECT_MATCHES, STRIP_SUBJECT_AND_SET_PARENT_DATA, new Action() {

				@Override
				public void execute(final Email email) {
					logger.debug("sending notification for email with id '{}'", email.getId());
					try {
						for (final EmailTemplate emailTemplate : service.getEmailTemplates(email)) {
							final Email notification = new Email();
							notification.setToAddresses(resolveRecipients(emailTemplate.getToAddresses(), email));
							notification.setCcAddresses(resolveRecipients(emailTemplate.getCCAddresses(), email));
							notification.setBccAddresses(resolveRecipients(emailTemplate.getBCCAddresses(), email));
							notification.setSubject(resolveText(emailTemplate.getSubject(), email));
							notification.setContent(resolveText(emailTemplate.getBody(), email));
							service.send(notification);
						}
					} catch (final Exception e) {
						logger.warn("error sending notification", e);
					}
				}

				private String resolveRecipients(final Iterable<String> recipients, final Email email) {
					// TODO use TemplateResolver with specific engine(s)
					return Joiner.on(EmailConstants.ADDRESSES_SEPARATOR) //
							.join(recipients);
				}

				private String resolveText(final String text, final Email email) {
					// TODO use TemplateResolver with specific engine(s)
					return text;
				}

			});

		}
		if (STORE_ATTACHMENTS.apply(task)) {
			logger.info(marker, "adding attachments action");
			readEmail.withAction(HAS_ATTACHMENTS, NO_ADAPTATIONS, new Action() {

				@Override
				public void execute(final Email email) {
					StoreDocument.newInstance() //
							.withDmsLogic(dmsLogic) //
							.withClassName(EMAIL_CLASS_NAME) //
							.withCardId(email.getId()) //
							// TODO category
							.withDocuments(documentsFrom(email.getAttachments())) //
							.build() //
							.execute();

				}

			});
		}
		if (START_PROCESS.apply(task)) {
			logger.info(marker, "adding start process action");
			readEmail.withAction(ALWAYS, NO_ADAPTATIONS, new Action() {

				@Override
				public void execute(final Email email) {
					final TemplateResolver templateResolver = TemplateResolverImpl.newInstance() //
							// TODO add engine for email
							.build();
					StartProcess.newInstance() //
							.withWorkflowLogic(workflowLogic) //
							.withHook(new Hook() {

								@Override
								public void started(final UserProcessInstance userProcessInstance) {
									email.setActivityId(userProcessInstance.getCardId());
									emailPersistence.save(email);

									if (task.isWorkflowAttachments()) {
										StoreDocument.newInstance() //
												.withDmsLogic(dmsLogic) //
												.withClassName(task.getWorkflowClassName()) //
												.withCardId(userProcessInstance.getCardId()) //
												// TODO category
												.withDocuments(documentsFrom(email.getAttachments())) //
												.build() //
												.execute();
									}
								}

							}) //
							.withTemplateResolver(templateResolver) //
							.withClassName(task.getWorkflowClassName()) //
							.withAttributes(task.getWorkflowAttributes()) //
							.withAdvanceStatus(task.isWorkflowAdvanceable()) //
							.build() //
							.execute();
				}

			});
		}

		final String name = task.getId().toString();
		return DefaultJob.newInstance() //
				.withName(name) //
				.withAction( //
						SafeCommand.of( //
								readEmail.build()) //
				) //
				.build();
	}

	private Iterable<Document> documentsFrom(final Iterable<Attachment> attachments) {
		return from(attachments) //
				.transform(new Function<Attachment, Document>() {

					@Override
					public Document apply(final Attachment input) {
						return new Document() {

							@Override
							public String getName() {
								return input.getName();
							}

							@Override
							public DataHandler getDataHandler() {
								return input.getDataHandler();
							}
						};
					}

				});
	}

	private EmailAccount emailAccountFor(final String emailAccountName) {
		logger.debug(marker, "getting email account for name '{}'", emailAccountName);
		for (final EmailAccount emailAccount : emailAccountStore.list()) {
			if (emailAccount.getName().equals(emailAccountName)) {
				return emailAccount;
			}
		}
		throw new IllegalArgumentException("email account not found");
	}

	private EmailConfiguration emailConfigurationFrom(final EmailAccount emailAccount) {
		logger.debug(marker, "getting email configuration from email account {}", emailAccount);
		return new EmailAccountConfiguration(emailAccount);
	}

	private Predicate<Email> predicate(final ReadEmailTask task) {
		logger.debug(marker, "creating main filter for email");
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				logger.debug(marker, "checking from address");
				final Pattern fromPattern = Pattern.compile(task.getRegexFromFilter());
				final Matcher fromMatcher = fromPattern.matcher(email.getFromAddress());
				if (!fromMatcher.matches()) {
					logger.debug(marker, "from address not matching");
					return false;
				}

				logger.debug(marker, "checking subject");
				final Pattern subjectPattern = Pattern.compile(task.getRegexSubjectFilter());
				final Matcher subjectMatcher = subjectPattern.matcher(email.getSubject());
				if (!subjectMatcher.matches()) {
					logger.debug(marker, "subject not matching");
					return false;
				}

				return true;
			}

		};
	}

}
