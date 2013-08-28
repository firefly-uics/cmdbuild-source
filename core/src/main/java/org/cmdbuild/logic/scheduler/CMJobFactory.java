package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobType;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.SchedulerJob;
import org.cmdbuild.services.scheduler.EmailServiceJob;
import org.cmdbuild.services.scheduler.StartProcessJob;

public class CMJobFactory {

	private final WorkflowLogic workflowLogic;

	public CMJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	/**
	 * Return the right CMJob looking for the type of the given ScheduldJob
	 * 
	 * If the type is unknown returns null
	 * 
	 * @param job
	 * @return
	 */
	public SchedulerJob from(final ScheduledJob job) {
		SchedulerJob theJob = null;

		if (job.getJobType().equals(ScheduledJobType.workflow)) {
			theJob = new StartProcessJob(job.getId(), workflowLogic);
			((StartProcessJob) theJob).setDetail(job.getDetail());
			((StartProcessJob) theJob).setParams(job.getParams());
		} else if (job.getJobType().equals(ScheduledJobType.emailService)) {
			theJob = new EmailServiceJob(job.getId());
		}

		return theJob;
	}

}
