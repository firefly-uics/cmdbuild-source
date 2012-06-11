package org.cmdbuild.workflow;


public interface CMActivityInstance {

	CMProcessInstance getProcessInstance();

	String getId();
	CMActivity getDefinition();
}
