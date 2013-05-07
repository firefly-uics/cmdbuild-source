package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.sql.DataSource;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.GraphProperties;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.GISLogic;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.cache.CachingLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.servlets.json.util.FlowStatusHelper;

public class JSONBaseWithSpringContext extends JSONBase {

	protected OperationUser operationUser() {
		return applicationContext().getBean(OperationUser.class);
	}

	/*
	 * Properties
	 */

	protected GraphProperties graphProperties() {
		return applicationContext().getBean(GraphProperties.class);
	}

	/*
	 * DataBase
	 */

	protected DataSource dataSource() {
		return applicationContext().getBean(DataSource.class);
	}

	protected CMDataView systemDataView() {
		return applicationContext().getBean(DBDataView.class);
	}

	protected CMDataView userDataView() {
		return applicationContext().getBean(UserDataView.class);
	}

	/*
	 * Stores
	 */

	protected FilterStore filterStore() {
		return applicationContext().getBean(FilterStore.class);
	}

	protected LookupStore lookupStore() {
		return applicationContext().getBean(LookupStore.class);
	}

	protected MenuStore menuStore() {
		return applicationContext().getBean(MenuStore.class);
	}

	/*
	 * Logics
	 */

	protected AuthenticationLogic authLogic() {
		return applicationContext().getBean(AuthenticationLogic.class);
	}

	protected CachingLogic cachingLogic() {
		return applicationContext().getBean(CachingLogic.class);
	}

	protected DataAccessLogic systemDataAccessLogic() {
		return applicationContext().getBean("systemDataAccessLogic", DataAccessLogic.class);
	}

	protected DataAccessLogic userDataAccessLogic() {
		return applicationContext().getBean("userDataAccessLogic", DataAccessLogic.class);
	}

	protected DataDefinitionLogic dataDefinitionLogic() {
		return applicationContext().getBean(DataDefinitionLogic.class);
	}

	protected DmsLogic dmsLogic() {
		return applicationContext().getBean(DmsLogic.class);
	}

	protected EmailLogic emailLogic() {
		return applicationContext().getBean(EmailLogic.class);
	}

	protected GISLogic gisLogic() {
		return applicationContext().getBean(GISLogic.class);
	}

	protected LookupLogic lookupLogic() {
		return applicationContext().getBean(LookupLogic.class);
	}

	protected SchedulerLogic schedulerLogic() {
		return applicationContext().getBean(SchedulerLogic.class);
	}

	protected SecurityLogic securityLogic() {
		return applicationContext().getBean(SecurityLogic.class);
	}

	protected ViewLogic viewLogic() {
		return applicationContext().getBean(ViewLogic.class);
	}

	protected WorkflowLogic workflowLogic() {
		return applicationContext().getBean(WorkflowLogic.class);
	}

	/*
	 * Utilities and helpers
	 */

	protected FlowStatusHelper flowStatusHelper() {
		return new FlowStatusHelper(lookupStore());
	}

}
