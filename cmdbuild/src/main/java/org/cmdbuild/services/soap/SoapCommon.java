package org.cmdbuild.services.soap;

import java.util.Collections;
import java.util.List;

import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

abstract class SoapCommon implements ApplicationContextAware {

	protected static final List<MetadataGroup> METADATA_NOT_SUPPORTED = Collections.emptyList();

	private ApplicationContext applicationContext;

	protected ApplicationContext applicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Deprecated
	protected UserContext getUserCtx() {
		// FIXME
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		return new OperationUserWrapper(as.getOperationUser());
	}

	private OperationUser getOperationUser() {
		// FIXME
		final DefaultAuthenticationService as = new DefaultAuthenticationService();
		return as.getOperationUser();
	}

	protected DmsLogic dmsLogic() {
		return applicationContext.getBean(DmsLogic.class);
	}

	protected LookupLogic lookupLogic() {
		return TemporaryObjectsBeforeSpringDI.getLookupLogic();
	}

	protected WorkflowLogicHelper workflowLogicHelper() {
		return new WorkflowLogicHelper(getUserCtx());
	}

}
