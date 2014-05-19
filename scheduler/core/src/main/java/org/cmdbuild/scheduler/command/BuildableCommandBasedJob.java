package org.cmdbuild.scheduler.command;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.scheduler.Job;

public class BuildableCommandBasedJob implements Job {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<BuildableCommandBasedJob> {

		private static final Command NULL = new Command() {

			@Override
			public void execute() {
				// nothing to do
			}

		};

		private String name;
		private Command action;

		private Builder() {
			// use factory method
		}

		@Override
		public BuildableCommandBasedJob build() {
			validate();
			return new BuildableCommandBasedJob(this);
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withAction(final Command action) {
			this.action = action;
			return this;
		}

		private void validate() {
			Validate.notBlank(name);
			action = defaultIfNull(action, NULL);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String name;
	private final Command action;

	private BuildableCommandBasedJob(final Builder builder) {
		this.name = builder.name;
		this.action = builder.action;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void execute() {
		action.execute();
	}

}
