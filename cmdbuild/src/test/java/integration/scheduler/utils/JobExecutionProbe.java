package integration.scheduler.utils;

import java.util.Date;

import org.cmdbuild.services.scheduler.trigger.*;
import org.hamcrest.Description;

import utils.async.Probe;


public abstract class JobExecutionProbe implements Probe {

	protected ExecutionListenerJob job;
	private final JobTrigger trigger;

	protected JobExecutionProbe(final JobTrigger trigger) {
		this.trigger = trigger;
	}

	public void setJob(ExecutionListenerJob job) {
		this.job = job;
	}

	public JobTrigger getTrigger() {
		return trigger;
	}

	public static JobExecutionProbe jobWasExecuted(final JobTrigger trigger) {
        return new JobExecutionProbe(trigger) {
        	private boolean wasExecuted = false;

			public void describeAcceptanceCriteriaTo(Description d) {
				d.appendText("job executed");
			}

			public void describeFailureTo(Description d) {
				d.appendText("job was not executed in time");
			}

			public boolean isSatisfied() {
				return wasExecuted;
			}

			public boolean isDone() {
				return wasExecuted;
			}

			public void sample() {
				wasExecuted = job.hasBeenExecuted();
			}
        };
    }

	public static JobExecutionProbe jobWasExecutedAfter(final OneTimeTrigger trigger) {
        return new JobExecutionProbe(trigger) {
        	private Date executionTime = null;
        	private final Date timeout = trigger.getDate();

			public void describeAcceptanceCriteriaTo(Description d) {
				d.appendText("job should have been executed after " + timeout.getTime());
			}

			public void describeFailureTo(Description d) {
				d.appendText("job was executed at " + executionTime.getTime());
			}

			public boolean isSatisfied() {
				return !executionTime.before(timeout);
			}

			public boolean isDone() {
				return (executionTime != null);
			}

			public void sample() {
				executionTime = job.getLastExecutionTime();
			}
        };
	}

	public static JobExecutionProbe jobExecutionCounter(final RecurringTrigger trigger, final int minTimes, final int maxTimes) {
        return new JobExecutionProbe(trigger) {
        	private int totalExecutions = 0;

			public void describeAcceptanceCriteriaTo(Description d) {
				d.appendText(String.format("job should have been executed between %d and %d times", minTimes, maxTimes));
			}

			public void describeFailureTo(Description d) {
				d.appendText(String.format("job was executed %d times", totalExecutions));
			}

			public boolean isSatisfied() {
				return (totalExecutions >= minTimes) && (totalExecutions <= maxTimes);
			}

			public boolean isDone() {
				return false;
			}

			public void sample() {
				totalExecutions = job.getTotalExecutions();
			}
        };
	}
}
