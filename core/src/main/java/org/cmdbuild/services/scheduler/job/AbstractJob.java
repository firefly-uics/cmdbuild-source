package org.cmdbuild.services.scheduler.job;

import java.util.Map;

public abstract class AbstractJob implements CMJob {

	private final String name;

	protected String detail;
	protected Map<String, String> params;

	AbstractJob(final Long id) {
		this.name = String.valueOf(id);
	}

	@Override
	public abstract void execute();

	@Override
	public String getName() {
		return name;
	}

	public void setDetail(final String detail) {
		this.detail = detail;
	}

	public void setParams(final Map<String, String> params) {
		this.params = params;
	}
}