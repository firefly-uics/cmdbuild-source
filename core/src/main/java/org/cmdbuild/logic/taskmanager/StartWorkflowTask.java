package org.cmdbuild.logic.taskmanager;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class StartWorkflowTask implements ScheduledTask {

	public static class Builder implements org.cmdbuild.common.Builder<StartWorkflowTask> {

		private Long id;
		private String description;
		private Boolean active;
		private String cronExpression;
		private String processClass;
		private Map<String, String> parameters;

		private Builder() {
			// use factory method
		}

		@Override
		public StartWorkflowTask build() {
			validate();
			return new StartWorkflowTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;
			parameters = (parameters == null) ? Collections.<String, String> emptyMap() : parameters;
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder withProcessClass(final String processClass) {
			this.processClass = processClass;
			return this;
		}

		public Builder withParameters(final Map<String, String> parameters) {
			this.parameters = parameters;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final String cronExpression;
	private final String processClass;
	private final Map<String, String> legacyParameters;

	private StartWorkflowTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.cronExpression = builder.cronExpression;
		this.processClass = builder.processClass;
		this.legacyParameters = Collections.unmodifiableMap(builder.parameters);
	}

	@Override
	public void accept(final TaskVistor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public String getCronExpression() {
		return cronExpression;
	}

	public String getProcessClass() {
		return processClass;
	}

	public Map<String, String> getParameters() {
		return legacyParameters;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
