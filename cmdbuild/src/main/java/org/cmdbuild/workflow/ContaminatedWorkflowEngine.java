package org.cmdbuild.workflow;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.elements.interfaces.CardQuery;

/**
 * All the ugliness of old filters should go here and be removed ASAP.
 */
@Legacy("Old DAO")
public interface ContaminatedWorkflowEngine extends CMWorkflowEngine {

	Iterable<CMProcessInstance> query(CardQuery cardQuery);
}
