package org.cmdbuild.logic.taskmanager.task.email;

import static com.google.common.base.Functions.identity;
import static com.google.common.base.Predicates.alwaysTrue;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Suppliers.memoize;
import static com.google.common.base.Suppliers.ofInstance;
import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.isEmpty;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.cmdbuild.common.template.engine.Engines.emptyStringOnNull;
import static org.cmdbuild.common.template.engine.Engines.map;
import static org.cmdbuild.common.template.engine.Engines.nullOnError;
import static org.cmdbuild.common.utils.guava.Suppliers.firstNotNull;
import static org.cmdbuild.common.utils.guava.Suppliers.nullOnException;
import static org.cmdbuild.data.store.Storables.storableOf;
import static org.cmdbuild.data.store.email.EmailConstants.EMAIL_CLASS_NAME;
import static org.cmdbuild.services.email.Predicates.named;
import static org.cmdbuild.services.template.engine.EngineNames.CARD_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.CQL_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.DB_TEMPLATE;
import static org.cmdbuild.services.template.engine.EngineNames.EMAIL_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.GROUP_USERS_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.MAPPER_PREFIX;
import static org.cmdbuild.services.template.engine.EngineNames.USER_PREFIX;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.template.TemplateResolver;
import org.cmdbuild.common.template.engine.EngineBasedTemplateResolver;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.StoreSupplier;
import org.cmdbuild.data.store.email.Attachment;
import org.cmdbuild.data.store.email.Email;
import org.cmdbuild.data.store.email.EmailConstants;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.StoreDocument;
import org.cmdbuild.logic.dms.StoreDocument.Document;
import org.cmdbuild.logic.email.EmailTemplateLogic;
import org.cmdbuild.logic.email.EmailTemplateLogic.Template;
import org.cmdbuild.logic.email.SendTemplateEmail;
import org.cmdbuild.logic.taskmanager.scheduler.AbstractJobFactory;
import org.cmdbuild.logic.taskmanager.task.email.mapper.EngineBasedMapper;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.StartProcess.Hook;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.email.EmailAccount;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.email.EmailServiceFactory;
import org.cmdbuild.services.email.SubjectHandler;
import org.cmdbuild.services.email.SubjectHandler.ParsedSubject;
import org.cmdbuild.services.template.engine.CardEngine;
import org.cmdbuild.services.template.engine.CqlEngine;
import org.cmdbuild.services.template.engine.DatabaseEngine;
import org.cmdbuild.services.template.engine.EmailEngine;
import org.cmdbuild.services.template.engine.GroupEmailEngine;
import org.cmdbuild.services.template.engine.GroupUsersEmailEngine;
import org.cmdbuild.services.template.engine.UserEmailEngine;
import org.cmdbuild.workflow.user.UserProcessInstance;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

public class ReadEmailTaskJobFactory extends AbstractJobFactory<ReadEmailTask> {

	private static final Predicate<Email> ALWAYS = alwaysTrue();

	private static enum TaskPredicate implements Predicate<ReadEmailTask> {

		SEND_NOTIFICATION() {

			@Override
			public boolean apply(final ReadEmailTask input) {
				return input.isNotificationActive();
			}

		}, //
		STORE_ATTACHMENTS() {

			@Override
			public boolean apply(final ReadEmailTask input) {
				return input.isAttachmentsActive();
			}

		}, //
		START_PROCESS() {

			@Override
			public boolean apply(final ReadEmailTask input) {
				return input.isWorkflowActive();
			}

		}, //
		;

	}

	private static final Predicate<Email> HAS_ATTACHMENTS = new Predicate<Email>() {

		@Override
		public boolean apply(final Email email) {
			return !isEmpty(email.getAttachments());
		}

	};

	private static final Function<Email, Email> IDENTITY = identity();

	private final Store<EmailAccount> emailAccountStore;
	private final EmailServiceFactory emailServiceFactory;
	private final SubjectHandler subjectHandler;
	private final Store<Email> emailStore;
	private final WorkflowLogic workflowLogic;
	private final DmsLogic dmsLogic;
	private final CMDataView dataView;
	private final EmailTemplateLogic emailTemplateLogic;
	private final DatabaseEngine databaseEngine;

	public ReadEmailTaskJobFactory( //
			final Store<EmailAccount> emailAccountStore, //
			final EmailServiceFactory emailServiceFactory, //
			final SubjectHandler subjectHandler, //
			final Store<Email> emailStore, //
			final WorkflowLogic workflowLogic, //
			final DmsLogic dmsLogic, //
			final CMDataView dataView, //
			final EmailTemplateLogic emailTemplateLogic, //
			final DatabaseEngine databaseEngine) {
		this.emailAccountStore = emailAccountStore;
		this.emailServiceFactory = emailServiceFactory;
		this.subjectHandler = subjectHandler;
		this.emailStore = emailStore;
		this.workflowLogic = workflowLogic;
		this.dmsLogic = dmsLogic;
		this.dataView = dataView;
		this.emailTemplateLogic = emailTemplateLogic;
		this.databaseEngine = databaseEngine;
	}

	private final Predicate<Email> SUBJECT_MATCHES = new Predicate<Email>() {

		@Override
		public boolean apply(final Email email) {
			final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
			if (!parsedSubject.hasExpectedFormat()) {
				return false;
			}

			try {
				emailStore.read(storableOf(parsedSubject.getEmailId()));
			} catch (final Exception e) {
				return false;
			}

			return true;
		}

	};
	private final Function<Email, Email> STRIP_SUBJECT_AND_SET_PARENT_DATA = new Function<Email, Email>() {

		@Override
		public Email apply(final Email email) {
			final ParsedSubject parsedSubject = subjectHandler.parse(email.getSubject());
			Validate.isTrue(parsedSubject.hasExpectedFormat(), "invalid subject format");
			final Email parentEmail = emailStore.read(storableOf(parsedSubject.getEmailId()));
			email.setSubject(parsedSubject.getRealSubject());
			email.setActivityId(parentEmail.getActivityId());
			email.setNotifyWith(parentEmail.getNotifyWith());
			return email;
		}

	};

	@Override
	protected Class<ReadEmailTask> getType() {
		return ReadEmailTask.class;
	}

	@Override
	protected Command command(final ReadEmailTask task) {
		final String emailAccountName = task.getEmailAccount();
		final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName);
		final EmailService service = emailServiceFactory.create(ofInstance(selectedEmailAccount));

		final ReadEmailCommand.Builder readEmail = ReadEmailCommand.newInstance() //
				.withEmailService(service) //
				.withEmailStore(emailStore) //
				.withPredicate(predicate(task));

		if (TaskPredicate.SEND_NOTIFICATION.apply(task)) {
			logger.info(marker, "adding notification action");
			readEmail.withAction(SUBJECT_MATCHES, STRIP_SUBJECT_AND_SET_PARENT_DATA, SafeAction.of(new Action() {

				@Override
				public void execute(final Email email) {
					final Supplier<Template> emailTemplateSupplier = memoize(new Supplier<Template>() {

						@Override
						public Template get() {
							final String name = defaultString(defaultIfBlank(task.getNotificationTemplate(),
									email.getNotifyWith()));
							return emailTemplateLogic.read(name);
						}

					});
					final Supplier<EmailAccount> templateEmailAccountSupplier = nullOnException(StoreSupplier.of(
							EmailAccount.class, emailAccountStore, named(emailTemplateSupplier.get().getAccount())));
					final Supplier<EmailAccount> taskEmailAccountSupplier = StoreSupplier.of(EmailAccount.class,
							emailAccountStore, named(task.getEmailAccount()));
					final Supplier<EmailAccount> emailAccountSupplier = firstNotNull(asList(
							templateEmailAccountSupplier, taskEmailAccountSupplier));
					final CMCard genericProcessCard = workflowLogic.getProcessInstance(dataView.getActivityClass()
							.getName(), email.getActivityId());
					final CMCard processCard = workflowLogic.getProcessInstance(genericProcessCard.getType().getName(),
							email.getActivityId());
					final EngineBasedTemplateResolver templateResolver = EngineBasedTemplateResolver.newInstance() //
							.withEngine(emptyStringOnNull(nullOnError( //
									UserEmailEngine.newInstance() //
											.withDataView(dataView) //
											.build())), //
									USER_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									GroupEmailEngine.newInstance() //
											.withDataView(dataView) //
											.build())), //
									GROUP_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									GroupUsersEmailEngine.newInstance() //
											.withDataView(dataView) //
											.withSeparator(EmailConstants.ADDRESSES_SEPARATOR) //
											.build() //
									)), //
									GROUP_USERS_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									EmailEngine.newInstance() //
											.withEmail(email) //
											.build())), //
									EMAIL_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError(map( //
									EngineBasedMapper.newInstance() //
											.withText(email.getContent()) //
											.withEngine(task.getMapperEngine()) //
											.build() //
											.map() //
									))), //
									MAPPER_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									CardEngine.newInstance() //
											.withCard(processCard) //
											.build() //
									)), //
									CARD_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									CqlEngine.newInstance() //
											.withDataView(dataView) //
											.build() //
									)), //
									CQL_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									databaseEngine //
									)), //
									DB_TEMPLATE) //
							.build();
					SendTemplateEmail.newInstance() //
							.withEmailAccountSupplier(emailAccountSupplier) //
							.withEmailServiceFactory(emailServiceFactory) //
							.withEmailTemplateSupplier(emailTemplateSupplier) //
							.withTemplateResolver(templateResolver) //
							.build() //
							.execute();
				}

			}));

		}
		if (TaskPredicate.STORE_ATTACHMENTS.apply(task)) {
			logger.info(marker, "adding attachments action");
			readEmail.withAction(HAS_ATTACHMENTS, IDENTITY, SafeAction.of(new Action() {

				@Override
				public void execute(final Email email) {
					StoreDocument.newInstance() //
							.withDmsLogic(dmsLogic) //
							.withClassName(EMAIL_CLASS_NAME) //
							.withCardId(email.getId()) //
							.withCategory(task.getAttachmentsCategory()) //
							.withDocuments(documentsFrom(email.getAttachments())) //
							.build() //
							.execute();
				}

			}));
		}
		if (TaskPredicate.START_PROCESS.apply(task)) {
			logger.info(marker, "adding start process action");
			readEmail.withAction(ALWAYS, IDENTITY, SafeAction.of(new Action() {

				@Override
				public void execute(final Email email) {
					final TemplateResolver templateResolver = EngineBasedTemplateResolver.newInstance() //
							.withEngine(emptyStringOnNull(nullOnError( //
									EmailEngine.newInstance() //
											.withEmail(email) //
											.build())), //
									EMAIL_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError(map( //
									EngineBasedMapper.newInstance() //
											.withText(email.getContent()) //
											.withEngine(task.getMapperEngine()) //
											.build() //
											.map() //
									))), //
									MAPPER_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									CqlEngine.newInstance() //
											.withDataView(dataView) //
											.build() //
									)), //
									CQL_PREFIX) //
							.withEngine(emptyStringOnNull(nullOnError( //
									databaseEngine //
									)), //
									DB_TEMPLATE) //
							.build();
					StartProcess.newInstance() //
							.withWorkflowLogic(workflowLogic) //
							.withHook(new Hook() {

								@Override
								public void started(final UserProcessInstance userProcessInstance) {
									email.setActivityId(userProcessInstance.getCardId());
									emailStore.update(email);

									if (task.isWorkflowAttachments()) {
										StoreDocument.newInstance() //
												.withDmsLogic(dmsLogic) //
												.withClassName(task.getWorkflowClassName()) //
												.withCardId(userProcessInstance.getCardId()) //
												.withCategory(task.getWorkflowAttachmentsCategory()) //
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

			}));

		}

		return readEmail.build();
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
		for (final EmailAccount emailAccount : emailAccountStore.readAll()) {
			if (emailAccount.getName().equals(emailAccountName)) {
				return emailAccount;
			}
		}
		throw new IllegalArgumentException("email account not found");
	}

	private Predicate<Email> predicate(final ReadEmailTask task) {
		logger.debug(marker, "creating main filter for email");
		return and(fromAddressMatches(task), subjectMatches(task));
	}

	private Predicate<Email> fromAddressMatches(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				logger.debug(marker, "checking from address");
				if (isEmpty(task.getRegexFromFilter())) {
					logger.debug(marker, "no from address filters");
					return true;
				}
				for (final String regex : task.getRegexFromFilter()) {
					final Pattern fromPattern = Pattern.compile(regex);
					final Matcher fromMatcher = fromPattern.matcher(email.getFromAddress());
					if (fromMatcher.matches()) {
						logger.debug(marker, "from address matches regex '{}'", regex);
						return true;
					}
				}
				logger.debug(marker, "from address not matching");
				return false;
			}

		};
	}

	private Predicate<Email> subjectMatches(final ReadEmailTask task) {
		return new Predicate<Email>() {

			@Override
			public boolean apply(final Email email) {
				logger.debug(marker, "checking subject");
				if (isEmpty(task.getRegexSubjectFilter())) {
					logger.debug(marker, "no subject filters");
					return true;
				}
				for (final String regex : task.getRegexSubjectFilter()) {
					final Pattern subjectPattern = Pattern.compile(regex);
					final Matcher subjectMatcher = subjectPattern.matcher(email.getSubject());
					if (subjectMatcher.matches()) {
						logger.debug(marker, "subject matches regex '{}'", regex);
						return true;
					}
				}
				logger.debug(marker, "subject not matching");
				return false;
			}

		};
	}

}
