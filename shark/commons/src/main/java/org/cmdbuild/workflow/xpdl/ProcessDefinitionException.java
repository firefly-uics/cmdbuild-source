package org.cmdbuild.workflow.xpdl;

public abstract class ProcessDefinitionException extends Exception {

	private static final long serialVersionUID = -780868577745391671L;

	public ProcessDefinitionException(Throwable cause) {
        super(cause);
    }
}