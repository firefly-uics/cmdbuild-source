package org.cmdbuild.workflow.user;

import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;

public interface UserWorkflowEngine extends CMWorkflowEngine {
	UserProcessClass findProcessClassById(Long id);
	UserProcessClass findProcessClassByName(String name);
	Iterable<UserProcessClass> findProcessClasses();
	Iterable<UserProcessClass> findAllProcessClasses();

	UserProcessInstance startProcess(CMProcessClass type) throws CMWorkflowException;
	UserProcessInstance advanceActivity(CMActivityInstance activityInstance) throws CMWorkflowException;
}
