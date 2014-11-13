package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.workflow.UserWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHelper {

	@Autowired
	private ApplicationContext applicationContext;

	public AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(SoapAuthenticationLogicBuilder.class).build();
	}

	public DmsLogic dmsLogic() {
		return applicationContext.getBean(PrivilegedDmsLogic.class);
	}

	public LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	public MenuLogic menuLogic() {
		return applicationContext.getBean(MenuLogic.class);
	}

	public MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

	public CMDataView systemDataView() {
		return applicationContext.getBean(DBDataView.class);
	}

	public DataAccessLogic userDataAccessLogic() {
		return applicationContext.getBean(UserDataAccessLogicBuilder.class).build();
	}

	public CMDataView userDataView() {
		return applicationContext.getBean("UserDataView", CMDataView.class);
	}

	public UserStore userStore() {
		return applicationContext.getBean(UserStore.class);
	}

	public WorkflowLogic userWorkflowLogic() {
		return applicationContext.getBean(UserWorkflowLogicBuilder.class).build();
	}

}
