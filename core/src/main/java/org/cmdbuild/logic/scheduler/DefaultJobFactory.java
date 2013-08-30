package org.cmdbuild.logic.scheduler;

import java.util.List;

import org.cmdbuild.config.EmailConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.data.store.scheduler.SchedulerJobParameterConverter;
import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.logic.email.EmailReceivingLogic;
import org.cmdbuild.logic.email.rules.AnswerToExistingMailFactory;
import org.cmdbuild.logic.email.rules.DownloadAttachmentsFactory;
import org.cmdbuild.logic.email.rules.StartWorkflowFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;
import org.cmdbuild.model.scheduler.SchedulerJobParameter;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.email.EmailAccountConfiguration;
import org.cmdbuild.services.email.EmailCallbackHandler.Rule;
import org.cmdbuild.services.email.EmailService;
import org.cmdbuild.services.scheduler.EmailServiceJob;
import org.cmdbuild.services.scheduler.StartProcessJob;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;

public class DefaultJobFactory implements JobFactory {

	private static class SchedulerJobConfiguration {

		private final Iterable<SchedulerJobParameter> parameters;

		public SchedulerJobConfiguration(final Iterable<SchedulerJobParameter> parameters) {
			this.parameters = parameters;
		}

		public String get(final String key) {
			logger.debug("getting key '{}'", key);
			for (final SchedulerJobParameter parameter : parameters) {
				if (parameter.getKey().equals(key)) {
					return parameter.getValue();
				}
			}
			logger.warn("key '{}' not found", key);
			return null;
		}

		public boolean getBoolean(final String key) {
			logger.debug("getting key '{}' as boolean", key);
			return Boolean.parseBoolean(get(key));
		}

	}

	private static final Logger logger = SchedulerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultJobFactory.class.getName());

	private final Notifier LOGGER_NOTIFIER = new Notifier() {

		@Override
		public void warn(final CMDBException e) {
			logger.warn("error while receiving email", e);
		}

	};

	private final WorkflowLogic workflowLogic;

	private final CMDataView dataView;
	private final ConfigurableEmailServiceFactory emailServiceFactory;
	private final AnswerToExistingMailFactory answerToExistingMailFactory;
	private final DownloadAttachmentsFactory downloadAttachmentsFactory;
	private final StartWorkflowFactory startWorkflowFactory;

	public DefaultJobFactory( //
			final WorkflowLogic workflowLogic, //
			final CMDataView systemDataView, //
			final ConfigurableEmailServiceFactory emailServiceFactory, //
			final AnswerToExistingMailFactory answerToExistingMailFactory, //
			final DownloadAttachmentsFactory downloadAttachmentsFactory, //
			final StartWorkflowFactory startWorkflowFactory //
	) {
		this.workflowLogic = workflowLogic;
		this.dataView = systemDataView;
		this.emailServiceFactory = emailServiceFactory;
		this.answerToExistingMailFactory = answerToExistingMailFactory;
		this.downloadAttachmentsFactory = downloadAttachmentsFactory;
		this.startWorkflowFactory = startWorkflowFactory;
	}

	@Override
	public Job create(final SchedulerJob schedulerJob) {
		final Job job;
		if (Type.workflow.equals(schedulerJob.getType())) {
			final StartProcessJob startProcessJob = new StartProcessJob(schedulerJob.getIdentifier(), workflowLogic);
			startProcessJob.setDetail(schedulerJob.getDetail());
			startProcessJob.setParams(schedulerJob.getLegacyParameters());
			job = startProcessJob;
		} else if (Type.emailService.equals(schedulerJob.getType())) {
			final SchedulerJobConfiguration configuration = parametersOf(schedulerJob);

			final String emailAccountName = configuration.get("email.account.name");
			final EmailAccount selectedEmailAccount = emailAccountFor(emailAccountName);
			final EmailConfiguration emailConfiguration = emailConfigurationFrom(selectedEmailAccount);
			final EmailService service = emailServiceFactory.create(emailConfiguration);

			final List<Rule> rules = Lists.newArrayList();
			if (configuration.getBoolean("rule.notification.active")) {
				rules.add(answerToExistingMailFactory.create(service));
			} else if (configuration.getBoolean("rule.attachments.active")) {
				logger.debug("adding attachments rule");
				rules.add(downloadAttachmentsFactory.create());
			} else if (configuration.getBoolean("rule.workflow.active")) {
				rules.add(startWorkflowFactory.create());
			}

			final EmailReceivingLogic emailReceivingLogic = new EmailReceivingLogic(service, rules, LOGGER_NOTIFIER);

			job = new EmailServiceJob(schedulerJob.getIdentifier(), emailReceivingLogic);
		} else {
			logger.warn(marker, "invalid type '{}'", schedulerJob.getType());
			job = null;
		}
		return job;
	}

	private SchedulerJobConfiguration parametersOf(final SchedulerJob schedulerJob) {
		logger.debug("getting parameters for job {}", schedulerJob);
		final StorableConverter<SchedulerJobParameter> schedulerJobParameterConverter = new SchedulerJobParameterConverter(
				schedulerJob.getId());
		final Store<SchedulerJobParameter> schedulerJobParameterStore = new DataViewStore<SchedulerJobParameter>(
				dataView, schedulerJobParameterConverter);
		final Iterable<SchedulerJobParameter> parameters = schedulerJobParameterStore.list();
		return new SchedulerJobConfiguration(parameters);
	}

	private EmailAccount emailAccountFor(final String emailAccountName) {
		logger.debug("getting email account for name '{}'", emailAccountName);
		final StorableConverter<EmailAccount> emailAccountConverter = new EmailAccountStorableConverter();
		final Store<EmailAccount> emailAccountStore = new DataViewStore<EmailAccount>(dataView, emailAccountConverter);
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

}
