package integration.scheduler.utils;

import java.util.Date;

import org.cmdbuild.services.scheduler.job.Job;

public class ExecutionListenerJob implements Job {

	private Date lastExecutionTime = null;
	private int totalExecutions = 0;

	public boolean hasBeenExecuted() {
		return (totalExecutions > 0);
	}

	public Date getLastExecutionTime() {
		return lastExecutionTime;
	}

	public int getTotalExecutions() {
		return totalExecutions;
	}

	public void execute() {
		lastExecutionTime = new Date();
		++totalExecutions;
	}

	public String getName() {
		return String.valueOf(this.hashCode());
	}
}
