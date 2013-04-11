package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.menu.MenuStore;
import org.springframework.context.ApplicationContext;

public class JSONBaseWithSpringContext extends JSONBase {

	private static ApplicationContext applicationContext = applicationContext();

	protected OperationUser operationUser() {
		return applicationContext.getBean(OperationUser.class);
	}

	/*
	 * Properties
	 */

	protected GraphProperties graphProperties() {
		return applicationContext.getBean(GraphProperties.class);
	}

	/*
	 * Stores
	 */

	protected FilterStore filterStore() {
		return applicationContext.getBean(FilterStore.class);
	}

	protected LookupStore lookupStore() {
		return applicationContext.getBean(LookupStore.class);
	}

	protected MenuStore menuStore() {
		return applicationContext.getBean(MenuStore.class);
	}

	/*
	 * Logics
	 */

	protected AuthenticationLogic authLogic() {
		return applicationContext.getBean(AuthenticationLogic.class);
	}

	protected DataAccessLogic dataAccessLogic() {
		return applicationContext.getBean(DataAccessLogic.class);
	}
	
	protected DataAccessLogic userDataAccessLogic() {
		return applicationContext.getBean("userDataAccessLogic", DataAccessLogic.class);
	}


	protected DataDefinitionLogic dataDefinitionLogic() {
		return applicationContext.getBean(DataDefinitionLogic.class);
	}

	protected DmsLogic dmsLogic() {
		return applicationContext.getBean(DmsLogic.class);
	}

	protected EmailLogic emailLogic() {
		return applicationContext.getBean(EmailLogic.class);
	}

	protected LookupLogic lookupLogic() {
		return applicationContext.getBean(LookupLogic.class);
	}

	protected SchedulerLogic schedulerLogic() {
		return applicationContext.getBean(SchedulerLogic.class);
	}

	protected SecurityLogic securityLogic() {
		return applicationContext.getBean(SecurityLogic.class);
	}

	protected WorkflowLogic workflowLogic() {
		return applicationContext.getBean(WorkflowLogic.class);
	}

}
