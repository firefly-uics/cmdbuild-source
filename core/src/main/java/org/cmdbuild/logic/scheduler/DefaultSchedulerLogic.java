package org.cmdbuild.logic.scheduler;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.Task;
import org.cmdbuild.logic.taskmanager.LogicAndSchedulerConverter;
import org.cmdbuild.scheduler.SchedulerService;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final SchedulerService schedulerService;
	private final Store<Task> store;
	private final LogicAndSchedulerConverter jobFactory;

	public DefaultSchedulerLogic( //
			final Store<Task> store, //
			final SchedulerService schedulerService, //
			final LogicAndSchedulerConverter jobFactory) {
		this.store = store;
		this.jobFactory = jobFactory;
		this.schedulerService = schedulerService;
	}

	@Override
	public Iterable<Task> findAllScheduledJobs() {
		logger.info("finding all scheduled jobs");
		return store.list();
	}

	@Override
	public Iterable<Task> findWorkflowJobsByProcess(final String classname) {
		throw new UnsupportedOperationException("TODO in the new way");
	}

	@Override
	public void startScheduler() {
		logger.info("starting scheduler service");
		schedulerService.start();
	}

	@Override
	public void stopScheduler() {
		logger.info("stopping scheduler service");
		schedulerService.stop();
	}

	@Override
	public void addAllScheduledJobs() {
		throw new UnsupportedOperationException("TODO in the new way");

	}

}
