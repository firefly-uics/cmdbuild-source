package integration.scheduler.utils;

import org.cmdbuild.services.scheduler.SchedulerService;

public class SelfRemovingJob extends ExecutionListenerJob {

	private SchedulerService scheduler;

	public SelfRemovingJob(SchedulerService scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public void execute() {
		scheduler.removeJob(this);
		super.execute();
	}
}
