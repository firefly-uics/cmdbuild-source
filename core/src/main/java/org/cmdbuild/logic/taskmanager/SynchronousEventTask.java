package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SynchronousEventTask implements Task {

	public static class Builder implements org.cmdbuild.common.Builder<SynchronousEventTask> {

		private Long id;
		private String description;
		private Boolean active;

		private Builder() {
			// use factory method
		}

		@Override
		public SynchronousEventTask build() {
			validate();
			return new SynchronousEventTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;
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

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;

	private SynchronousEventTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
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
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
