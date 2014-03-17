package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.services.scheduler.StartProcessJob;

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
		final StartProcessJob startProcessJob = new StartProcessJob(name, workflowLogic);
		startProcessJob.setDetail(task.getProcessClass());
		startProcessJob.setParams(task.getParameters());
		return startProcessJob;
	}

}
