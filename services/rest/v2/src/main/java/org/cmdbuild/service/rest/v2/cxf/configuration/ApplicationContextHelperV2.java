package org.cmdbuild.service.rest.v2.cxf.configuration;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.DefaultAuthenticationService.Configuration;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.NotSystemUserFetcher;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.DefaultAuthenticationLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.dms.DmsLogic;
import org.cmdbuild.logic.dms.PrivilegedDmsLogic;
import org.cmdbuild.logic.menu.MenuLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.workflow.UserWorkflowLogicBuilder;
import org.cmdbuild.logic.workflow.WorkflowLogic;
import org.cmdbuild.privileges.DBGroupFetcher;
import org.cmdbuild.services.meta.MetadataStoreFactory;
import org.cmdbuild.workflow.LookupHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ApplicationContextHelperV2 {

	@Autowired
	private ApplicationContext applicationContext;

	public AuthenticationLogic authenticationLogic() {
		/*
		 * TODO
		 * 
		 * it could be DefaultAuthenticationLogic but at the moment we don't
		 * want other authenticators than database one
		 */
		final LegacyDBAuthenticator databaseAuthenticator = applicationContext.getBean(LegacyDBAuthenticator.class);
		final NotSystemUserFetcher notSystemUserFetcher = applicationContext.getBean(NotSystemUserFetcher.class);
		final DBGroupFetcher dbGroupFetcher = applicationContext.getBean(DBGroupFetcher.class);
		final DefaultAuthenticationService authenticationService = new DefaultAuthenticationService(
				new Configuration() {

					@Override
					public Collection<String> getActiveAuthenticators() {
						return asList(databaseAuthenticator.getName());
					}

				}, systemDataView());
		authenticationService.setPasswordAuthenticators(databaseAuthenticator);
		authenticationService.setUserFetchers(databaseAuthenticator, notSystemUserFetcher);
		authenticationService.setGroupFetcher(dbGroupFetcher);
		authenticationService.setUserStore(userStore());

		final PrivilegeContextFactory privilegeContextFactory = applicationContext
				.getBean(PrivilegeContextFactory.class);

		return new DefaultAuthenticationLogic(authenticationService, privilegeContextFactory, systemDataView());
	}

	public DmsLogic dmsLogic() {
		return applicationContext.getBean(PrivilegedDmsLogic.class);
	}

	public LookupHelper lookupHelper() {
		return applicationContext.getBean(LookupHelper.class);
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

	public SecurityLogic securityLogic() {
		return applicationContext.getBean(SecurityLogic.class);
	}

	public DataAccessLogic systemDataAccessLogic() {
		return applicationContext.getBean(SystemDataAccessLogicBuilder.class).build();
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
