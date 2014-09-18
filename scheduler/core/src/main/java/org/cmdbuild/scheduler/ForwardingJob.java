package org.cmdbuild.scheduler;

public abstract class ForwardingJob implements Job {

	private final Job delegate;

	protected ForwardingJob(final Job delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void execute() {
		delegate.execute();
	}

}
