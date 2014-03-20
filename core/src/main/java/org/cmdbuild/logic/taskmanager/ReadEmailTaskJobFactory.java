package org.cmdbuild.logic.taskmanager;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.logic.email.rules.AnswerToExistingMailFactory;
import org.cmdbuild.logic.email.rules.DownloadAttachmentsFactory;
import org.cmdbuild.logic.email.rules.PropertiesMapper;
import org.cmdbuild.logic.email.rules.StartWorkflow.Configuration;
import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;
import org.cmdbuild.logic.email.rules.StartWorkflowFactory;
import org.cmdbuild.logic.scheduler.RuleWithAdditionalCondition;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailAccountConfiguration;
import org.cmdbuild.services.email.EmailCallbackHandler.Applicable;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.scheduler.EmailServiceJob;

import com.google.common.collect.Lists;

public class ReadEmailTaskJobFactory extends AbstractJobFactory<ReadEmailTask> {

	private static final Notifier LOGGER_NOTIFIER = new Notifier() {

		@Override
		public void warn(final CMDBException e) {
			logger.warn(marker, "error while receiving email", e);
		}

	};

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
			rules.add(ruleWithGlobalCondition(answerToExistingMailFactory.create(service), task));
		}
		if (task.isAttachmentsRuleActive()) {
			logger.info(marker, "adding attachments rule");
			rules.add(ruleWithGlobalCondition(downloadAttachmentsFactory.create(), task));
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
			rules.add(ruleWithGlobalCondition(startWorkflowFactory.create(_configuration), task));
		}

		final EmailReceivingLogic emailReceivingLogic = new EmailReceivingLogic(service, rules, LOGGER_NOTIFIER);

		final String name = task.getId().toString();
		return new EmailServiceJob(name, emailReceivingLogic);
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

	private Rule ruleWithGlobalCondition(final Rule rule, final ReadEmailTask schedulerJob) {
		logger.debug(marker, "creating rule with global condition");
		final String fromExpression = schedulerJob.getRegexFromFilter();
		final String subjectExpression = schedulerJob.getRegexSubjectFilter();
		final Applicable applicable = new Applicable() {

			@Override
			public boolean applies(final Email email) {
				logger.debug(marker, "checking from address");
				final Pattern fromPattern = Pattern.compile(fromExpression);
				final Matcher fromMatcher = fromPattern.matcher(email.getFromAddress());
				if (!fromMatcher.matches()) {
					logger.debug(marker, "from address not matching");
					return false;
				}

				logger.debug(marker, "checking subject");
				final Pattern subjectPattern = Pattern.compile(subjectExpression);
				final Matcher subjectMatcher = subjectPattern.matcher(email.getSubject());
				if (!subjectMatcher.matches()) {
					logger.debug(marker, "subject not matching");
					return false;
				}

				return rule.applies(email);
			}

			@Override
			public String toString() {
				return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
						.append("from", schedulerJob.getRegexFromFilter()) //
						.append("subject", schedulerJob.getRegexFromFilter()) //
						.toString();
			}

		};
		return new RuleWithAdditionalCondition(rule, applicable);
	}

}
