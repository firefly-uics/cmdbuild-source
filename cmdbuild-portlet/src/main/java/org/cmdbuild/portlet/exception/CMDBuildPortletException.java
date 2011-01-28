package org.cmdbuild.portlet.exception;

public abstract class CMDBuildPortletException extends RuntimeException {

    protected String[] parameters;

    public abstract String getExceptionTypeText();

    public String[] getExceptionParameters() {
        return parameters;
    }
}
