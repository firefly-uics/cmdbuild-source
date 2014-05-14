package org.cmdbuild.logic.taskmanager;

import static org.cmdbuild.scheduler.command.SafeCommand.safe;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.command.BuildableCommandBasedJob;
import org.cmdbuild.scheduler.command.Command;
import org.cmdbuild.services.sync.store.Store;
import org.cmdbuild.services.sync.store.StoreSynchronizer;
import org.cmdbuild.services.sync.store.internal.Catalog;
import org.cmdbuild.services.sync.store.internal.InternalStore;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	private static class ConnectorTaskCommandWrapper implements Command {

		private final CMDataView dataView;
		private final ConnectorTask task;

		private ConnectorTaskCommandWrapper(final CMDataView dataView, final ConnectorTask task) {
			this.dataView = dataView;
			this.task = task;
		}

		@Override
		public void execute() {
			final Catalog catalog = null; // TODO
			final Store left = null; // TODO
			final Store rightAndTarget = InternalStore.newInstance() //
					.withDataView(dataView) //
					.withCatalog(catalog) //
					.build();
			StoreSynchronizer.newInstance() //
					.withLeft(left) //
					.withRight(rightAndTarget) //
					.withTarget(rightAndTarget) //
					.build() //
					.sync();
		}

	}

	private final CMDataView dataView;

	public ConnectorTaskJobFactory(final CMDataView dataView) {
		this.dataView = dataView;
	}

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Job doCreate(final ConnectorTask task) {
		final String name = task.getId().toString();
		final Command command = new ConnectorTaskCommandWrapper(dataView, task);
		return BuildableCommandBasedJob.newInstance() //
				.withName(name) //
				.withAction(safe(command)) //
				.build();
	}

}
