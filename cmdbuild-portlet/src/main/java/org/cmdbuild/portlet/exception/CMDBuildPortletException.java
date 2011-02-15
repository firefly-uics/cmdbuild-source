package org.cmdbuild.portlet.exception;

public abstract class CMDBuildPortletException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected String[] parameters;

	public abstract String getExceptionTypeText();

	public String[] getExceptionParameters() {
		return parameters;
	}
}
