package org.cmdbuild.config;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.scheduler.DefaultScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobType;

public class AfterPropertiesSave implements PropertiesVisitor {

	final private CMDataView dataView;
	final private SchedulerLogic schedulerLogic;

	public AfterPropertiesSave(final CMDataView dataView, final SchedulerLogic schedulerLogic) {
		this.dataView = dataView;
		this.schedulerLogic = schedulerLogic;
	}

	@Override
	public void visit(final AuthProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final CmdbfProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final CmdbuildProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final DatabaseProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final DmsProperties properties) {
		// nothing to do
	}

	/**
	 * Looks if the email service is well configured
	 * AKA: has a IMAP server name, and a delay time
	 * 
	 * If it is configured, update or create the
	 * Quartz job setting the relative CRON expression.
	 * Otherwise remove the job from DB and Quartz
	 * 
	 */
	@Override
	public void visit(final EmailProperties properties) {
		final ScheduledJob emailJob = findEmailServiceScheduledJob();
		if (emailServiceIsConfigued(properties)) {
			final Integer delay = properties.emailServiceDelay();
			final String cronExpression = String.format("0 0/%s * 1/1 * ? *", delay);

			if (emailJob != null) {

				Log.CMDBUILD.info(String.format("EMAIL SERVICE: Update configuration to run every %s minutes", delay));

				final ScheduledJob jobToUpdate = DefaultScheduledJob.newScheduledJob(emailJob)
						.withCronExpression(cronExpression)
						.build();

				schedulerLogic.update(jobToUpdate);
			} else {

				Log.CMDBUILD.info(String.format("EMAIL SERVICE: Start - Configured to run every %s minutes", delay));

				final ScheduledJob jobToSave = DefaultScheduledJob.newRunningEmailServiceJob()
						.withCronExpression(cronExpression)
						.withDetail("CM_EMAIL_SERVICE_JOB")
						.build();

				schedulerLogic.createAndStart(jobToSave);
			}

		} else {
			Log.CMDBUILD.info("EMAIL SERVICE: Stop");

			if (emailJob != null) {
				schedulerLogic.delete(emailJob.getId());
			}
		}
	}

	@Override
	public void visit(final GisProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final GraphProperties properties) {
		// nothing to do
	}

	@Override
	public void visit(final WorkflowProperties properties) {
		// nothing to do
	}

	/*
	 * The email service must be consider
	 * as configured if there are an IMAP server name,
	 * and the delay to wait for check the inBox
	 */
	private boolean emailServiceIsConfigued(final EmailProperties properties) {
		final Integer delay = properties.emailServiceDelay();

		return (delay != null
					&& properties.isImapConfigured());
	}

	private ScheduledJob findEmailServiceScheduledJob() {
		ScheduledJob emailJob = null;
		final Iterable<ScheduledJob> jobs = schedulerLogic.findAllScheduledJobs();
		for (final ScheduledJob job: jobs) {
			if (ScheduledJobType.emailService.equals(job.getJobType())) {
				emailJob = job;
				break;
			}
		}

		return emailJob;
	}
}
