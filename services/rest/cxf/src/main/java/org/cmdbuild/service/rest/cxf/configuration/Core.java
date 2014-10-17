package org.cmdbuild.service.rest.cxf.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Supplier;

@Configuration
public class Core {

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

	@Bean
	public DataAccessLogic userDataAccessLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemDataAccessLogicBuilder.class).build();
	}

	@Bean
	public LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	@Bean
	public MenuLogic menuLogic() {
		return applicationContext.getBean(MenuLogic.class);
	}

	@Bean
	public WorkflowLogic userWorkflowLogic() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(SystemWorkflowLogicBuilder.class).build();
	}

	@Bean
	public CMDataView userDataView() {
		// TODO change class when authentication will be implemented
		return applicationContext.getBean(DBDataView.class);
	}

	@Bean
	public CMDataView systemDataView() {
		return applicationContext.getBean(DBDataView.class);
	}

	@Bean
	public MetadataStoreFactory metadataStoreFactory() {
		return applicationContext.getBean(MetadataStoreFactory.class);
	}

	@Bean
	public AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(SoapAuthenticationLogicBuilder.class).build();
	}

}
