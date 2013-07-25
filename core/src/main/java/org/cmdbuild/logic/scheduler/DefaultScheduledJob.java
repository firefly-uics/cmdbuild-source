package org.cmdbuild.logic.scheduler;

import java.util.Map;

import org.apache.commons.lang.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJob;
import org.cmdbuild.logic.scheduler.SchedulerLogic.ScheduledJobType;

public class DefaultScheduledJob implements ScheduledJob {

	public static class ScheduledJobBuilder implements Builder<DefaultScheduledJob> {

		private String cronExpression;
		private String detail;
		private Long jobId;
		private String description;
		private Map<String, String> params;
		private final ScheduledJobType type;
		private final boolean running;

		private ScheduledJobBuilder(final ScheduledJobType type, final boolean running) {
			this.type = type;
			this.running = running;
		}

		private ScheduledJobBuilder(final ScheduledJobType type) {
			this(type, false);
		}

		@Override
		public DefaultScheduledJob build() {
			Validate.notNull(cronExpression);
			Validate.notNull(detail);

			return new DefaultScheduledJob(this);
		}

		public ScheduledJobBuilder withDetail(final String detail) {
			this.detail = detail;
			return this;
		}

		public ScheduledJobBuilder withParams(final Map<String, String> params) {
			this.params = params;
			return this;
		}

		public ScheduledJobBuilder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public ScheduledJobBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public ScheduledJobBuilder withId(final Long jobId) {
			this.jobId = jobId;
			return this;
		}
	}

	public static ScheduledJobBuilder newRunningWorkflowJob() {
		return new ScheduledJobBuilder(ScheduledJobType.workflow, true);
	}

	public static ScheduledJobBuilder newWorkflowJob() {
		return new ScheduledJobBuilder(ScheduledJobType.workflow);
	}

	public static ScheduledJobBuilder newRunningEmailServiceJob() {
		return new ScheduledJobBuilder(ScheduledJobType.emailService, true);
	}

	public static ScheduledJobBuilder newEmailServiceJob() {
		return new ScheduledJobBuilder(ScheduledJobType.emailService);
	}

	public static ScheduledJobBuilder newScheduledJob(final String type, final boolean running) {
		ScheduledJobBuilder jb = null;
		if (ScheduledJobType.workflow.toString().equals(type)) {
			jb = running ? newRunningWorkflowJob() : newWorkflowJob();
		} else if (ScheduledJobType.emailService.toString().equals(type)) {
			jb = running ? newRunningEmailServiceJob() : newEmailServiceJob();
		}

		return jb;
	}

	public static ScheduledJobBuilder newScheduledJob(final ScheduledJob job) {
		return new ScheduledJobBuilder(job.getJobType(), job.isRunning())
			.withId(job.getId())
			.withCronExpression(job.getCronExpression())
			.withDetail(job.getDetail())
			.withDescription(job.getDescription())
			.withParams(job.getParams());
	}

	private final String cronExpression;
	private final String detail;
	private final Long jobId;
	private final String description;
	private final ScheduledJobType type;
	private final boolean running;
	private final Map<String, String> params;

	private DefaultScheduledJob(final ScheduledJobBuilder scheduledJobBuilder) {
		this.cronExpression = scheduledJobBuilder.cronExpression;
		this.detail = scheduledJobBuilder.detail;
		this.jobId = scheduledJobBuilder.jobId;
		this.description = scheduledJobBuilder.description;
		this.type = scheduledJobBuilder.type;
		this.running = scheduledJobBuilder.running;
		this.params = scheduledJobBuilder.params;
	}

	@Override
	public Long getId() {
		return jobId;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public String getDetail() {
		return detail;
	}

	@Override
	public Map<String, String> getParams() {
		return params;
	}

	@Override
	public ScheduledJobType getJobType() {
		return type;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

}
