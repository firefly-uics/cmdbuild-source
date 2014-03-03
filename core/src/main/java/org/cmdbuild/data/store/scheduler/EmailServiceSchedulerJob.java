package org.cmdbuild.data.store.scheduler;

public class EmailServiceSchedulerJob extends SchedulerJob {

	public EmailServiceSchedulerJob(final Long id) {
		super(id);
	}

	@Override
	public void accept(final SchedulerJobVisitor visitor) {
		visitor.visit(this);
	}

}
