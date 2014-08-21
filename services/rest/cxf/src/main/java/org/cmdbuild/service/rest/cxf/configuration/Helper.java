package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Supplier;

@Configuration
public class Helper {

	@Autowired
	private ApplicationContext applicationContext;

	public Supplier<String> currentGroupNameSupplier() {
		return new Supplier<String>() {

			@Override
			public String get() {
				return applicationContext.getBean(UserStore.class).getUser().getPreferredGroup().getName();
			}

		};
	}

	public DataAccessLogic userDataAccessLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemDataAccessLogicBuilder.class).build();
	}

	public LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	public MenuLogic menuLogic() {
		return applicationContext.getBean(MenuLogic.class);
	}

	public WorkflowLogic userWorkflowLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemWorkflowLogicBuilder.class).build();
	}

	public CMDataView userDataView() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(DBDataView.class);
	}

	public CMDataView systemDataView() {
		return applicationContext.getBean(DBDataView.class);
	}

	public MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

}
