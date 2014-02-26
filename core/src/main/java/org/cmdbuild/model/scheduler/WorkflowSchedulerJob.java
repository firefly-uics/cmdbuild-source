package org.cmdbuild.model.scheduler;

public class WorkflowSchedulerJob extends SchedulerJob {

	public WorkflowSchedulerJob() {
		super();
	}

	public WorkflowSchedulerJob(final Long id) {
		super(id);
	}

	@Override
	public void accept(final SchedulerJobVisitor visitor) {
		visitor.visit(this);
	}

}
