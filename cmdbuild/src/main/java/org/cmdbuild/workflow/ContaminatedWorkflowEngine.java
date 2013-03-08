package org.cmdbuild.workflow;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.interfaces.CardQuery;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserWorkflowEngine;

/**
 * All the ugliness of old filters should go here and be removed ASAP.
 */
@Legacy("Old DAO")
public interface ContaminatedWorkflowEngine extends UserWorkflowEngine {

	Iterable<UserProcessInstance> query(CardQuery cardQuery);

	UserProcessInstance findProcessInstance(CMProcessClass processClass, Long cardId);
}
