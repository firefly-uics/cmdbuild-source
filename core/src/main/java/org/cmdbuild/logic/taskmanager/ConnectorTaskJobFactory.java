package org.cmdbuild.logic.taskmanager;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter.AbstractJobFactory;
import org.cmdbuild.scheduler.Job;

public class ConnectorTaskJobFactory extends AbstractJobFactory<ConnectorTask> {

	@Override
	protected Class<ConnectorTask> getType() {
		return ConnectorTask.class;
	}

	@Override
	protected Job doCreate(final ConnectorTask task) {
		throw new UnsupportedOperationException("TODO");
	}

}
