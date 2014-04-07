package org.cmdbuild.logic.scheduler;

import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobType;
import org.cmdbuild.services.scheduler.job.CMJob;
import org.cmdbuild.services.scheduler.job.EmailServiceJob;
import org.cmdbuild.services.scheduler.job.StartProcessJob;

public class CMJobFactory {
	/**
	 * Return the right CMJob looking
	 * for the type of the given ScheduldJob
	 * 
	 * If the type is unknown returns null
	 * 
	 * @param job
	 * @return
	 */
	public static CMJob from(final ScheduledJob job) {
		CMJob theJob = null;

		if (job.getJobType().equals(ScheduledJobType.workflow)) {
			theJob = new StartProcessJob(job.getId());
			((StartProcessJob)theJob).setDetail(job.getDetail());
			((StartProcessJob)theJob).setParams(job.getParams());
		} else if (job.getJobType().equals(ScheduledJobType.emailService)) {
			theJob = new EmailServiceJob(job.getId());
		}

		return theJob;
	}
}
