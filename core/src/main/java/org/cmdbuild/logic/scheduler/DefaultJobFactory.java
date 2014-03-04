package org.cmdbuild.logic.scheduler;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobVisitor;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.logic.email.rules.AnswerToExistingMailFactory;
import org.cmdbuild.logic.email.rules.DownloadAttachmentsFactory;
import org.cmdbuild.logic.email.rules.PropertiesMapper;
import org.cmdbuild.logic.email.rules.StartWorkflow.Configuration;
import org.cmdbuild.logic.email.rules.StartWorkflow.Mapper;
import org.cmdbuild.logic.email.rules.StartWorkflowFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.email.Email;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailAccountConfiguration;
import org.cmdbuild.services.email.EmailCallbackHandler.Applicable;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.scheduler.EmailServiceJob;
import org.cmdbuild.services.scheduler.StartProcessJob;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class DefaultJobFactory implements JobFactory {

	private static final Logger logger = SchedulerLogic.logger;

	private final Notifier LOGGER_NOTIFIER = new Notifier() {

		@Override
		public void warn(final CMDBException e) {
			logger.warn("error while receiving email", e);
		}

	};

	private final WorkflowLogic workflowLogic;

	private final Store<EmailAccount> emailAccountStore;
	private final ConfigurableEmailServiceFactory emailServiceFactory;
	private final AnswerToExistingMailFactory answerToExistingMailFactory;
	private final DownloadAttachmentsFactory downloadAttachmentsFactory;
	private final StartWorkflowFactory startWorkflowFactory;

	public DefaultJobFactory( //
			final WorkflowLogic workflowLogic, //
			final Store<EmailAccount> emailAccountStore, //
			final ConfigurableEmailServiceFactory emailServiceFactory, //
			final AnswerToExistingMailFactory answerToExistingMailFactory, //
			final DownloadAttachmentsFactory downloadAttachmentsFactory, //
			final StartWorkflowFactory startWorkflowFactory //
	) {
		this.workflowLogic = workflowLogic;
		this.emailAccountStore = emailAccountStore;
		this.emailServiceFactory = emailServiceFactory;
		this.answerToExistingMailFactory = answerToExistingMailFactory;
		this.downloadAttachmentsFactory = downloadAttachmentsFactory;
		this.startWorkflowFactory = startWorkflowFactory;
	}

	@Override
	public Job create(final SchedulerJob schedulerJob) {
		logger.info("creating job from '{}'", schedulerJob);
		return new SchedulerJobVisitor() {

			private Job job;

			public Job create() {
				schedulerJob.accept(this);
				Validate.notNull(job, "job cannot be created");
				return job;
			}

			@Override
			public void visit(final EmailServiceSchedulerJob schedulerJob) {
				final String emailAccountName = schedulerJob.getEmailAccount();
				final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName);
				final EmailConfiguration emailConfiguration = emailConfigurationFrom(selectedEmailAccount);
				final EmailService service = emailServiceFactory.create(emailConfiguration);

				final List<Rule> rules = Lists.newArrayList();
				if (schedulerJob.isNotificationActive()) {
					logger.info("adding notification rule");
					rules.add(ruleWithGlobalCondition(answerToExistingMailFactory.create(service), schedulerJob));
				}
				if (schedulerJob.isAttachmentsActive()) {
					logger.info("adding attachments rule");
					rules.add(ruleWithGlobalCondition(downloadAttachmentsFactory.create(), schedulerJob));
				}
				if (schedulerJob.isWorkflowActive()) {
					logger.info("adding start process rule");
					final String className = schedulerJob.getWorkflowClassName();
					final String mapping = schedulerJob.getWorkflowFieldsMapping();
					final boolean advance = schedulerJob.isWorkflowAdvanceable();
					final boolean saveAttachments = schedulerJob.isAttachmentsStorableToWorkflow();
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
					rules.add(ruleWithGlobalCondition(startWorkflowFactory.create(_configuration), schedulerJob));
				}

				final EmailReceivingLogic emailReceivingLogic = new EmailReceivingLogic(service, rules, LOGGER_NOTIFIER);

				job = new EmailServiceJob(schedulerJob.getIdentifier(), emailReceivingLogic);
			}

			private EmailAccount emailAccountFor(final String emailAccountName) {
				logger.debug("getting email account for name '{}'", emailAccountName);
				for (final EmailAccount emailAccount : emailAccountStore.list()) {
					if (emailAccount.getName().equals(emailAccountName)) {
						return emailAccount;
					}
				}
				throw new IllegalArgumentException("email account not found");
			}

			private EmailConfiguration emailConfigurationFrom(final EmailAccount emailAccount) {
				logger.debug("getting email configuration from email account {}", emailAccount);
				return new EmailAccountConfiguration(emailAccount);
			}

			private Rule ruleWithGlobalCondition(final Rule rule, final EmailServiceSchedulerJob schedulerJob) {
				logger.debug("creating rule with global condition");
				final String fromExpression = schedulerJob.getRegexFromFilter();
				final String subjectExpression = schedulerJob.getRegexSubjectFilter();
				final Applicable applicable = new Applicable() {

					@Override
					public boolean applies(final Email email) {
						logger.debug("checking from address");
						final Pattern fromPattern = Pattern.compile(fromExpression);
						final Matcher fromMatcher = fromPattern.matcher(email.getFromAddress());
						if (!fromMatcher.matches()) {
							logger.debug("from address not matching");
							return false;
						}

						logger.debug("checking subject");
						final Pattern subjectPattern = Pattern.compile(subjectExpression);
						final Matcher subjectMatcher = subjectPattern.matcher(email.getSubject());
						if (!subjectMatcher.matches()) {
							logger.debug("subject not matching");
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

			@Override
			public void visit(final WorkflowSchedulerJob schedulerJob) {
				final StartProcessJob startProcessJob = new StartProcessJob(schedulerJob.getIdentifier(), workflowLogic);
				startProcessJob.setDetail(schedulerJob.getProcessClass());
				startProcessJob.setParams(schedulerJob.getParameters());
				job = startProcessJob;
			}

		}.create();
	}

}
