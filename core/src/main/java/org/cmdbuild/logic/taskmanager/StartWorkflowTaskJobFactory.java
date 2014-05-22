package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.Action;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.logic.workflow.StartProcess;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.scheduler.command.Command;

public class StartWorkflowTaskJobFactory extends AbstractJobFactory<StartWorkflowTask> {

	private static class StartProcessCommandWrapper implements Command {

		public static StartProcessCommandWrapper of(final Action delegate) {
			return new StartProcessCommandWrapper(delegate);
		}

		private final Action delegate;

		private StartProcessCommandWrapper(final Action delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute() {
			delegate.execute();
		}

	}

	private final WorkflowLogic workflowLogic;

	public StartWorkflowTaskJobFactory(final WorkflowLogic workflowLogic) {
		this.workflowLogic = workflowLogic;
	}

	@Override
	protected Class<StartWorkflowTask> getType() {
		return StartWorkflowTask.class;
	}

	@Override
	protected Command command(final StartWorkflowTask task) {
		final StartProcess startProcess = StartProcess.newInstance() //
				.withWorkflowLogic(workflowLogic) //
				.withClassName(task.getProcessClass()) //
				.withAttributes(task.getAttributes()) //
				.build();
		return StartProcessCommandWrapper.of(startProcess);
	}

}
