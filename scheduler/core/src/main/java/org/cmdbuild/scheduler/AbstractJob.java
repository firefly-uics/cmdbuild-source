package org.cmdbuild.scheduler;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.Validate;

public abstract class AbstractJob implements Job {

	private final String name;

	protected AbstractJob(final String name) {
		Validate.isTrue(isNotBlank(name), "name cannot be null/empty/blank");
		this.name = name;
	}

	@Override
	public abstract void execute();

	@Override
	public String getName() {
		return name;
	}

}