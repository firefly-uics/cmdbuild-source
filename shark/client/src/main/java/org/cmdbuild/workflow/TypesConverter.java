package org.cmdbuild.workflow;

public interface TypesConverter {

	Object toWorkflowType(Object obj);
	Object fromWorkflowType(Object obj);
}
