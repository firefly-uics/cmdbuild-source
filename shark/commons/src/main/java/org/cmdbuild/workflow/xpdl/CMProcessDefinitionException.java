package org.cmdbuild.workflow.xpdl;

import org.cmdbuild.workflow.CMWorkflowException;

public class CMProcessDefinitionException extends CMWorkflowException {

	private static final long serialVersionUID = -780868577745391671L;

	public CMProcessDefinitionException(final Throwable nativeException) {
        super(nativeException);
    }
}