package org.cmdbuild.workflow;


public interface CMActivityInstance {

	CMProcessInstance getProcessInstance();

	String getId();
	CMActivity getDefinition() throws CMWorkflowException;
	String getPerformerName();
}
