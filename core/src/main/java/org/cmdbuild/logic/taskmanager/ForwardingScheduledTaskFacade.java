package org.cmdbuild.logic.taskmanager;

public abstract class ForwardingScheduledTaskFacade implements ScheduledTaskFacade {

	private final ScheduledTaskFacade delegate;

	public ForwardingScheduledTaskFacade(final ScheduledTaskFacade delegate) {
		this.delegate = delegate;
	}

	@Override
	public Long create(final ScheduledTask task) {
		return delegate.create(task);
	}

	@Override
	public Iterable<ScheduledTask> read() {
		return delegate.read();
	}

	@Override
	public ScheduledTask read(final ScheduledTask task) {
		return delegate.read(task);
	}

	@Override
	public void update(final ScheduledTask task) {
		delegate.update(task);
	}

	@Override
	public void delete(final ScheduledTask task) {
		delegate.delete(task);
	}

}
