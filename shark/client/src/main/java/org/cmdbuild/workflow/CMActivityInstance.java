package org.cmdbuild.workflow;


public interface CMActivityInstance {

	CMProcessInstance getProcessInstance();

	Object getId();
	CMActivity getDefinition();
}
