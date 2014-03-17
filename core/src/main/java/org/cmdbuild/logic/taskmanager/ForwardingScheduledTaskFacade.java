package org.cmdbuild.logic.taskmanager;

public abstract class ForwardingScheduledTaskFacade implements SchedulerFacade {

	private final SchedulerFacade delegate;

	public ForwardingScheduledTaskFacade(final SchedulerFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	public void create(final ScheduledTask task) {
		delegate.create(task);
	}

	@Override
	public void delete(final ScheduledTask task) {
		delegate.delete(task);
	}

}
