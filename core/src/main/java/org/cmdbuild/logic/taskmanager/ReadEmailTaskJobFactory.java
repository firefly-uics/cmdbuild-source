package org.cmdbuild.logic.taskmanager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailAccountConfiguration;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.scheduler.DefaultJob;
import org.cmdbuild.services.scheduler.SafeCommand;
import org.cmdbuild.services.scheduler.reademail.AnswerToExistingMailFactory;
import org.cmdbuild.services.scheduler.reademail.DownloadAttachmentsFactory;
import org.cmdbuild.services.scheduler.reademail.PropertiesMapper;
import org.cmdbuild.services.scheduler.reademail.ReadEmail;
import org.cmdbuild.services.scheduler.reademail.Rule;
import org.cmdbuild.services.scheduler.reademail.StartWorkflow.Configuration;
import org.cmdbuild.services.scheduler.reademail.StartWorkflow.Mapper;
import org.cmdbuild.services.scheduler.reademail.StartWorkflowFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

public class ReadEmailTaskJobFactory extends AbstractJobFactory<ReadEmailTask> {

	private final Store<EmailAccount> emailAccountStore;
	private final ConfigurableEmailServiceFactory emailServiceFactory;
	private final AnswerToExistingMailFactory answerToExistingMailFactory;
	private final DownloadAttachmentsFactory downloadAttachmentsFactory;
	private final StartWorkflowFactory startWorkflowFactory;

	public ReadEmailTaskJobFactory( //
			final Store<EmailAccount> emailAccountStore, //
			final ConfigurableEmailServiceFactory emailServiceFactory, //
			final AnswerToExistingMailFactory answerToExistingMailFactory, //
			final DownloadAttachmentsFactory downloadAttachmentsFactory, //
			final StartWorkflowFactory startWorkflowFactory //
	) {
		this.emailAccountStore = emailAccountStore;
		this.emailServiceFactory = emailServiceFactory;
		this.answerToExistingMailFactory = answerToExistingMailFactory;
		this.downloadAttachmentsFactory = downloadAttachmentsFactory;
		this.startWorkflowFactory = startWorkflowFactory;
	}

	@Override
	protected Class<ReadEmailTask> getType() {
		return ReadEmailTask.class;
	}

	@Override
	protected Job doCreate(final ReadEmailTask task) {
		final String emailAccountName = task.getEmailAccount();
		final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName);
		final EmailConfiguration emailConfiguration = emailConfigurationFrom(selectedEmailAccount);
		final EmailService service = emailServiceFactory.create(emailConfiguration);

		final List<Rule> rules = Lists.newArrayList();
		if (task.isNotificationRuleActive()) {
			logger.info(marker, "adding notification rule");
			rules.add(answerToExistingMailFactory.create(service));
		}
		if (task.isAttachmentsRuleActive()) {
			logger.info(marker, "adding attachments rule");
			rules.add(downloadAttachmentsFactory.create());
		}
		if (task.isWorkflowRuleActive()) {
			logger.info(marker, "adding start process rule");
			final String className = task.getWorkflowClassName();
			final String mapping = task.getWorkflowFieldsMapping();
			final boolean advance = task.isWorkflowAdvanceable();
			final boolean saveAttachments = task.isWorkflowAttachments();
			final Configuration _configuration = new Configuration() {

				@Override
				public String getClassName() {
					return className;
				}

				@Override
				public Mapper getMapper() {
					return new PropertiesMapper(mapping);
				}

				@Override
				public boolean advance() {
					return advance;
				}

				@Override
				public boolean saveAttachments() {
					return saveAttachments;
				}

			};
			rules.add(startWorkflowFactory.create(_configuration));
		}

		final String name = task.getId().toString();
		return DefaultJob.newInstance() //
				.withName(name) //
				.withAction( //
						SafeCommand.of( //
								ReadEmail.newInstance() //
										.withEmailService(service) //
										.withPredicate(predicate(task)) //
										.withRules(rules) //
										.build()) //
				) //
				.build();
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
