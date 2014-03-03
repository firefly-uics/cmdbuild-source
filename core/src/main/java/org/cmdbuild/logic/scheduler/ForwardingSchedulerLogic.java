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
	public Iterable<SchedulerJob> findWorkflowJobsByProcess(final String classname) {
		return inner.findWorkflowJobsByProcess(classname);
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
