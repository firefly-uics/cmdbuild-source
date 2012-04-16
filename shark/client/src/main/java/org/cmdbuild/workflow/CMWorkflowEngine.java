package org.cmdbuild.workflow;


public interface CMWorkflowEngine {

	CMProcessClass findProcessClass(Object idOrName);
	//CMProcessClass findProcessClassById(Object id);
	//CMProcessClass findProcessClassByName(String name);
}
