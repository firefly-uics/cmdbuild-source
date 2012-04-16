package org.cmdbuild.workflow;

public class CMWorkflowException extends Exception {

	private static final long serialVersionUID = -7807268444851679193L;

	public CMWorkflowException(final Throwable nativeException) {
		super(nativeException);
	}
}
