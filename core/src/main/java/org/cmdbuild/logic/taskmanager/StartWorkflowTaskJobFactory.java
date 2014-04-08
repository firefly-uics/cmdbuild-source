package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.scheduler.DefaultJob;
import org.cmdbuild.services.scheduler.SafeCommand;
import org.cmdbuild.services.scheduler.startprocess.StartProcess;

public class StartWorkflowTaskJobFactory extends AbstractJobFactory<StartWorkflowTask> {

	private final WorkflowLogic workflowLogic;

	public StartWorkflowTaskJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	@Override
	protected Class<StartWorkflowTask> getType() {
		return StartWorkflowTask.class;
	}

	@Override
	protected Job doCreate(final StartWorkflowTask task) {
		final String name = task.getId().toString();
		return DefaultJob.newInstance() //
				.withName(name) //
				.withAction( //
						SafeCommand.of( //
								StartProcess.newInstance() //
										.withWorkflowLogic(workflowLogic) //
										.withClassName(task.getProcessClass()) //
										.withAttributes(task.getAttributes()) //
										.build() //
								) //
				) //
				.build();
	}

}
