package org.cmdbuild.scheduler;

import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.Map;

import org.apache.commons.lang.Validate;

public abstract class AbstractJob implements Job {

	private final String name;

	protected String detail;
	protected Map<String, String> params;

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

	public void setDetail(final String detail) {
		this.detail = detail;
	}

	public void setParams(final Map<String, String> params) {
		this.params = params;
	}

}