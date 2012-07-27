package org.cmdbuild.workflow.user;

import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;

public interface UserWorkflowEngine extends CMWorkflowEngine {
	UserProcessInstance startProcess(CMProcessClass type) throws CMWorkflowException;
	UserProcessInstance advanceActivity(CMActivityInstance activityInstance) throws CMWorkflowException;
}
