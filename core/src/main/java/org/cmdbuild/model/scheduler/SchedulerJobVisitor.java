package org.cmdbuild.model.scheduler;

public interface SchedulerJobVisitor {

	void visit(final EmailServiceSchedulerJob schedulerJob);

	void visit(final WorkflowSchedulerJob schedulerJob);

}
