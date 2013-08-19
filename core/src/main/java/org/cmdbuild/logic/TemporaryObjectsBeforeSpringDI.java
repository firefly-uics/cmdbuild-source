package org.cmdbuild.logic;

import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.logic.workflow.WorkflowLogicBuilder;
import org.cmdbuild.spring.annotations.CmdbuildComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@Legacy("Spring should be used")
@CmdbuildComponent
public class TemporaryObjectsBeforeSpringDI {

	@Autowired
	@Qualifier("system")
	public static CMDataView dataView;

	@Autowired
	@Qualifier("system")
	public static WorkflowLogicBuilder workflowLogicBuilder;

	public static CMDataView getSystemView() {
		return dataView;
	}

	public static WorkflowLogic getSystemWorkflowLogic() {
		return workflowLogicBuilder.build();
	}

}
