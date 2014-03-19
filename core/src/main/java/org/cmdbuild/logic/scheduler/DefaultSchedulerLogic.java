package org.cmdbuild.logic.scheduler;

import java.util.Collections;

import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.Task;
import org.cmdbuild.scheduler.SchedulerService;

public class DefaultSchedulerLogic implements SchedulerLogic {

	private final SchedulerService schedulerService;
	private final Store<Task> store;

	public DefaultSchedulerLogic(final Store<Task> store, final SchedulerService schedulerService) {
		this.store = store;
		this.schedulerService = schedulerService;
	}

	@Override
	public Iterable<Task> findAllScheduledJobs() {
		logger.info("finding all scheduled jobs");
		return store.list();
	}

	@Override
	public Iterable<Task> findWorkflowJobsByProcess(final String classname) {
		// TODO TODO in the new way
		return Collections.emptyList();
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
		// TODO TODO in the new way
	}

}
