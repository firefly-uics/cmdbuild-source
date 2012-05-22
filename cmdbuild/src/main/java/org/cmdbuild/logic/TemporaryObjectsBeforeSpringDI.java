package org.cmdbuild.logic;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.CMAccessControlManager;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.dao.driver.CachingDriver;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.auth.AccessControlManagerWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.store.MapDashboardStore;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.WorkflowEngineWrapper;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.xpdl.XpdlManager;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	static XpdlManager.GroupQueryAdapter gca = new XpdlManager.GroupQueryAdapter() {
		@Override
		public String[] getAllGroupNames() {
			List<String> names = new ArrayList<String>();
			for (GroupCard groupCard : GroupCard.all()) {
				names.add(groupCard.getName());
			}
			return names.toArray(new String[names.size()]);
		}
	};

	final static CachingDriver driver;
	final static CMWorkflowService workflowService;
	final static ProcessDefinitionManager processDefinitionManager;

	static {
		final javax.sql.DataSource datasource = DBService.getInstance().getDataSource();
		driver = new PostgresDriver(datasource);
		workflowService = new RemoteSharkService(WorkflowProperties.getInstance());
		processDefinitionManager = new XpdlManager(workflowService, gca);
	}

	public static CachingDriver getDriver() {
		return driver;
	}

	public static DashboardLogic getDashboardLogic(UserContext userCtx) {
		return new DashboardLogic(getUserContextView(userCtx), new MapDashboardStore());
	}

	public static CMDataView getUserContextView(UserContext userCtx) {
		final CMAccessControlManager acm = new AccessControlManagerWrapper(userCtx.privileges());
		return new UserDataView(new DBDataView(driver), acm);
	}

	public static CMWorkflowEngine getWorkflowEngine(UserContext userCtx) {
		return new WorkflowEngineWrapper(userCtx, processDefinitionManager);
	}
}
