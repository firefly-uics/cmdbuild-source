package org.cmdbuild.service.rest.cxf;

import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.service.rest.serialization.ErrorHandler;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public abstract class CxfService {

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ErrorHandler errorHandler;

	protected ErrorHandler errorHandler() {
		return errorHandler;
	}

	protected DataAccessLogic userDataAccessLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemDataAccessLogicBuilder.class).build();
	}

	protected LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	protected WorkflowLogic userWorkflowLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemWorkflowLogicBuilder.class).build();
	}

	protected CMDataView userDataView() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(DBDataView.class);
	}

	protected CMDataView systemDataView() {
		return applicationContext.getBean(DBDataView.class);
	}

	protected MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

}
