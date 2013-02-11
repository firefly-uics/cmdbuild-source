package org.cmdbuild.logic;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.DBGroupFetcher;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.LegacyDBAuthenticator;
import org.cmdbuild.auth.context.DefaultPrivilegeContextFactory;
import org.cmdbuild.common.annotations.Legacy;
import org.cmdbuild.config.WorkflowProperties;
import org.cmdbuild.dao.driver.AbstractDBDriver;
import org.cmdbuild.dao.driver.AbstractDBDriver.DefaultTypeObjectCache;
import org.cmdbuild.dao.driver.postgres.PostgresDriver;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.logger.WorkflowLogger;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.data.DataAccessLogic;
import org.cmdbuild.logic.data.DataDefinitionLogic;
import org.cmdbuild.logic.privileges.SecurityLogic;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.DBTemplateService;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.TemplateRepository;
import org.cmdbuild.services.auth.Group;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.store.DBDashboardStore;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.services.store.FilterStore;
import org.cmdbuild.workflow.ContaminatedWorkflowEngine;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.SharkTypesConverter;
import org.cmdbuild.workflow.UpdateOperationListenerImpl;
import org.cmdbuild.workflow.WorkflowEngineWrapper;
import org.cmdbuild.workflow.WorkflowEventManagerImpl;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.widget.CalendarWidgetFactory;
import org.cmdbuild.workflow.widget.CreateModifyCardWidgetFactory;
import org.cmdbuild.workflow.widget.LinkCardsWidgetFactory;
import org.cmdbuild.workflow.widget.ManageEmailWidgetFactory;
import org.cmdbuild.workflow.widget.ManageRelationWidgetFactory;
import org.cmdbuild.workflow.widget.OpenAttachmentWidgetFactory;
import org.cmdbuild.workflow.widget.OpenNoteWidgetFactory;
import org.cmdbuild.workflow.widget.OpenReportWidgetFactory;
import org.cmdbuild.workflow.widget.WebServiceWidgetFactory;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlManager;
import org.cmdbuild.workflow.xpdl.XpdlProcessDefinitionStore;

@Legacy("Spring should be used")
public class TemporaryObjectsBeforeSpringDI {

	static XpdlManager.GroupQueryAdapter gca = new XpdlManager.GroupQueryAdapter() {
		@Override
		public String[] getAllGroupNames() {
			final List<String> names = new ArrayList<String>();
			for (final GroupCard groupCard : GroupCard.all()) {
				names.add(groupCard.getName());
			}
			return names.toArray(new String[names.size()]);
		}
	};

	private static final AbstractDBDriver driver;
	private static final DBDataView dbDataView;
	private static final DefaultPrivilegeContextFactory privilegeCtxFactory;
	private static final AbstractSharkService workflowService;
	private static final ProcessDefinitionManager processDefinitionManager;
	private static final WorkflowLogger workflowLogger;
	private static final WorkflowEventManager workflowEventManager;
	private static final WorkflowTypesConverter workflowTypesConverter;
	private static final AuthenticationLogic authLogic;

	static {
		final javax.sql.DataSource datasource = DBService.getInstance().getDataSource();
		driver = new PostgresDriver(datasource, new DefaultTypeObjectCache());
		dbDataView = new DBDataView(driver);
		privilegeCtxFactory = new DefaultPrivilegeContextFactory();
		authLogic = instantiateAuthenticationLogic();
		workflowLogger = new WorkflowLogger();
		workflowService = new RemoteSharkService(WorkflowProperties.getInstance());
		processDefinitionManager = new XpdlManager(workflowService, gca, newXpdlProcessDefinitionStore(workflowService));
		workflowTypesConverter = new SharkTypesConverter(dbDataView);
		workflowEventManager = new WorkflowEventManagerImpl(workflowService, workflowTypesConverter,
				processDefinitionManager);

		workflowService.setUpdateOperationListener(new UpdateOperationListenerImpl(workflowEventManager));
	}

	private static AuthenticationLogic instantiateAuthenticationLogic() {
		final AuthenticationService authService = new DefaultAuthenticationService();
		authService.setUserStore(new SessionVars());
		authService.setGroupFetcher(new DBGroupFetcher(dbDataView));
		final LegacyDBAuthenticator authenticator = new LegacyDBAuthenticator(dbDataView);
		authService.setPasswordAuthenticators(authenticator);
		authService.setUserFetchers(authenticator);
		final AuthenticationLogic authLogic = new AuthenticationLogic(authService);
		return authLogic;
	}

	private static XpdlProcessDefinitionStore newXpdlProcessDefinitionStore(final CMWorkflowService workflowService) {
		return new XpdlProcessDefinitionStore(workflowService, newXpdlVariableFactory(), newXpdlWidgetFactory());
	}

	private static XpdlExtendedAttributeVariableFactory newXpdlVariableFactory() {
		return new SharkStyleXpdlExtendedAttributeVariableFactory();
	}

	private static XpdlExtendedAttributeWidgetFactory newXpdlWidgetFactory() {
		final ValuePairXpdlExtendedAttributeWidgetFactory factory = new ValuePairXpdlExtendedAttributeWidgetFactory();

		factory.addWidgetFactory(new CalendarWidgetFactory(getTemplateRepository()));
		factory.addWidgetFactory(new CreateModifyCardWidgetFactory(getTemplateRepository(), getSystemDataAccessLogic()));
		factory.addWidgetFactory(new LinkCardsWidgetFactory(getTemplateRepository(), getSystemDataAccessLogic()));
		factory.addWidgetFactory(new ManageRelationWidgetFactory(getTemplateRepository(), getSystemDataAccessLogic()));
		factory.addWidgetFactory(new ManageEmailWidgetFactory(getTemplateRepository(), getSystemEmailLogic()));
		factory.addWidgetFactory(new OpenAttachmentWidgetFactory(getTemplateRepository()));
		factory.addWidgetFactory(new OpenNoteWidgetFactory(getTemplateRepository()));
		factory.addWidgetFactory(new OpenReportWidgetFactory(getTemplateRepository()));
		factory.addWidgetFactory(new WebServiceWidgetFactory(getTemplateRepository()));

		return factory;
	}

	public static AbstractDBDriver getDriver() {
		return driver;
	}

	public static DefaultPrivilegeContextFactory getPrivilegeContextFactory() {
		return privilegeCtxFactory;
	}

	public static AuthenticationLogic getAuthenticationLogic() {
		return authLogic;
	}

	public static FilterStore getFilterStore() {
		return new DataViewFilterStore(getSystemView(), new SessionVars().getUser());
	}

	public static DashboardLogic getDashboardLogic() {
		return new DashboardLogic(getUserDataView(), new DBDashboardStore(), new SimplifiedUserContext(new SessionVars().getCurrentUserContext()));
	}

	public static GISLogic getGISLogic() {
		return new GISLogic(UserContext.systemContext());
	}

	public static CMDataView getUserDataView() {
		return new UserDataView(new DBDataView(driver), new SessionVars().getUser());
	}

	public static CMDataView getSystemView() {
		return dbDataView;
	}

	public static DataAccessLogic getDataAccessLogic() {
		return new DataAccessLogic(getUserDataView());
	}

	public static DataDefinitionLogic getDataDefinitionLogic() {
		return new DataDefinitionLogic(getUserDataView());
	}

	public static DataAccessLogic getSystemDataAccessLogic() {
		return new DataAccessLogic(getSystemView());
	}

	public static WorkflowLogic getWorkflowLogic() {
		return new WorkflowLogic(getWorkflowEngine(new SessionVars().getCurrentUserContext()));
	}

	public static WorkflowLogic getSystemWorkflowLogic() {
		return new WorkflowLogic(getWorkflowEngine(UserContext.systemContext()));
	}

	public static ContaminatedWorkflowEngine getWorkflowEngine(final UserContext userCtx) {
		final WorkflowEngineWrapper workflowEngine = new WorkflowEngineWrapper(userCtx, workflowService,
				workflowTypesConverter, processDefinitionManager);
		workflowEngine.setEventListener(workflowLogger);
		return workflowEngine;
	}

	public static WorkflowEventManager getWorkflowEventManager() {
		return workflowEventManager;
	}

	public static ProcessDefinitionManager getProcessDefinitionManager() {
		return processDefinitionManager;
	}

	public static CMWorkflowService getWorkflowService() {
		return workflowService;
	}

	public static WorkflowTypesConverter getWorkflowTypesConverter() {
		return workflowTypesConverter;
	}

	public static class SimplifiedUserContext {
		private final UserContext userContext;

		public SimplifiedUserContext(final UserContext userContext) {
			this.userContext = userContext;
		}

		public List<String> getGroupNames() {
			final List<String> groupNames = new ArrayList<String>();
			for (final Group g : userContext.getGroups()) {
				groupNames.add(g.getName());
			}
			return groupNames;
		}

		public boolean isAdmin() {
			return userContext.privileges().isAdmin();
		}
	}

	public static SecurityLogic getSecurityLogic() {
		return new SecurityLogic(getSystemView());
	}

	public static EmailLogic getEmailLogic(final UserContext userContext) {
		return new EmailLogic(userContext);
	}

	private static EmailLogic getSystemEmailLogic() {
		return getEmailLogic(UserContext.systemContext());
	}

	private static TemplateRepository getTemplateRepository() {
		return new DBTemplateService();
	}

}
