package org.cmdbuild.data.store.scheduler;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public abstract class SchedulerJob implements Storable {

	private final Long id;

	private String description;
	private String cronExpression;
	private boolean running;

	protected SchedulerJob() {
		this(null);
	}

	protected SchedulerJob(final Long id) {
		this.id = id;
	}

	public abstract void accept(final SchedulerJobVisitor visitor);

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	public void setCronExpression(final String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(final Boolean running) {
		this.running = (running == null) ? false : running;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
