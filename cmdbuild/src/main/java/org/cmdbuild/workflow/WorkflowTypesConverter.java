package org.cmdbuild.workflow;

public interface WorkflowTypesConverter {

	Object toWorkflowType(Object obj);

	Object fromWorkflowType(Object obj);
}
