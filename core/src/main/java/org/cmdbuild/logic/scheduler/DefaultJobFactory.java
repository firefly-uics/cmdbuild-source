package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.model.scheduler.SchedulerJob.Type;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.scheduler.EmailServiceJob;
import org.cmdbuild.services.scheduler.StartProcessJob;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultJobFactory implements JobFactory {

	private static final Logger logger = SchedulerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultJobFactory.class.getName());

	private final WorkflowLogic workflowLogic;

	public DefaultJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
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
			job = new EmailServiceJob(schedulerJob.getIdentifier());
		} else {
			logger.warn(marker, "invalid type '{}'", schedulerJob.getType());
			job = null;
		}
		return job;
	}

}
