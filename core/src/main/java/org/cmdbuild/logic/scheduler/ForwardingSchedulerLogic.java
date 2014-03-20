package org.cmdbuild.logic.scheduler;

import org.cmdbuild.data.store.task.Task;

public abstract class ForwardingSchedulerLogic implements SchedulerLogic {

	private final SchedulerLogic inner;

	protected ForwardingSchedulerLogic(final SchedulerLogic inner) {
		this.inner = inner;
	}

	@Override
	public Iterable<Task> findAllScheduledJobs() {
		return inner.findAllScheduledJobs();
	}

	@Override
	public Iterable<Task> findWorkflowJobsByProcess(final String classname) {
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
