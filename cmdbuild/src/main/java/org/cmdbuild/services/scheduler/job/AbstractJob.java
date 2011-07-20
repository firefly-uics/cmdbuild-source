package org.cmdbuild.services.scheduler.job;

import java.util.Map;


public abstract class AbstractJob implements Job {

	private String name;

	protected String detail;
	protected Map<String, String> params;

	AbstractJob(int id) {
		this.name = String.valueOf(id);
	}

	public String getName() {
		return name;
	}

	public void setDetail(String detail) {
		this.detail = detail;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

	public abstract void execute();

}
