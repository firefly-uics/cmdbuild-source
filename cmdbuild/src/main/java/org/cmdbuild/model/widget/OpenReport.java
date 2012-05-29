package org.cmdbuild.model.widget;

import java.util.Map;

public class OpenReport extends Widget {

	private String reportCode;
	private String forceFormat;
	private Map<String,String> preset;

	public void setReportCode(final String reportCode) {
		this.reportCode = reportCode;
	}

	public String getReportCode() {
		return reportCode;
	}

	public void setForceFormat(final String forceFormat) {
		this.forceFormat = forceFormat;
	}

	public String getForceFormat() {
		return forceFormat;
	}	

	public void setPreset(final Map<String,String> preset) {
		this.preset = preset;
	}

	public Map<String,String> getPreset() {
		return preset;
	}
}
