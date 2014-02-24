package org.cmdbuild.logic.scheduler;

import org.cmdbuild.model.scheduler.SchedulerJob;

public class ForwardingSchedulerLogic implements SchedulerLogic {

	private final SchedulerLogic inner;

	public ForwardingSchedulerLogic(final SchedulerLogic inner) {
		this.inner = inner;
	}

	@Override
	public Iterable<SchedulerJob> findAllScheduledJobs() {
		return inner.findAllScheduledJobs();
	}

	@Override
	public Iterable<SchedulerJob> findJobsByDetail(final String detail) {
		return inner.findJobsByDetail(detail);
	}

	@Override
	public SchedulerJob update(final SchedulerJob job) {
		return inner.update(job);
	}

	@Override
	public void delete(final Long jobId) {
		inner.delete(jobId);
	}

	@Override
	public void startScheduler() {
		inner.startScheduler();
	}

	@Override
	public void stopScheduler() {
		inner.stopScheduler();
	}

	@Override
	public void addAllScheduledJobs() {
		inner.addAllScheduledJobs();
	}

}
