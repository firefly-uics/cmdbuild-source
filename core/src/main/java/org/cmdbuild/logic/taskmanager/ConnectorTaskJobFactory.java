package org.cmdbuild.logic.taskmanager;

import static org.cmdbuild.scheduler.command.SafeCommand.safe;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private static class ConnectorTaskCommandWrapper implements Command {

		public static ConnectorTaskCommandWrapper of(final ConnectorTask delegate) {
			return new ConnectorTaskCommandWrapper(delegate);
		}

		private final ConnectorTask delegate;

		private ConnectorTaskCommandWrapper(final ConnectorTask delegate) {
			this.delegate = delegate;
		}

		@Override
		public void execute() {
			// TODO
		}

	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Job doCreate(final ConnectorTask task) {
		final String name = task.getId().toString();
		return BuildableCommandBasedJob.newInstance() //
				.withName(name) //
				.withAction(safe(ConnectorTaskCommandWrapper.of(task))) //
				.build();
	}
}
