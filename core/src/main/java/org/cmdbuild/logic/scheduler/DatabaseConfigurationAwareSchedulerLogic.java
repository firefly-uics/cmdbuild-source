package org.cmdbuild.logic.scheduler;

import org.cmdbuild.config.DatabaseConfiguration;

public class DatabaseConfigurationAwareSchedulerLogic extends ForwardingSchedulerLogic {

	private final DatabaseConfiguration databaseConfiguration;

	public DatabaseConfigurationAwareSchedulerLogic(final SchedulerLogic inner,
			final DatabaseConfiguration databaseConfiguration) {
		super(inner);
		this.databaseConfiguration = databaseConfiguration;
	}

	@Override
	public void addAllScheduledJobs() {
		if (!databaseConfiguration.isConfigured()) {
			logger.warn("database not configured");
			return;
		}
		super.addAllScheduledJobs();
	}

}
