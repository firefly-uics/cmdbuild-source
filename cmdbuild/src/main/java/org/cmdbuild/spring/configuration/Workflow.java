package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logger.WorkflowLogger;
import org.cmdbuild.logic.email.EmailLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.services.TemplateRepository;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.cmdbuild.workflow.DataViewWorkflowPersistence.DataViewWorkflowPersistenceBuilder;
import org.cmdbuild.workflow.DefaultGroupQueryAdapter;
import org.cmdbuild.workflow.DefaultWorkflowEngine;
import org.cmdbuild.workflow.DefaultWorkflowEngine.DefaultWorkflowEngineBuilder;
import org.cmdbuild.workflow.DefaultXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.SharkTypesConverter.SharkTypesConverterBuilder;
import org.cmdbuild.workflow.UpdateOperationListenerImpl;
import org.cmdbuild.workflow.WorkflowEventManagerImpl;
import org.cmdbuild.workflow.WorkflowPersistence;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.AbstractSharkService;
import org.cmdbuild.workflow.service.AbstractSharkService.UpdateOperationListener;
import org.cmdbuild.workflow.service.RemoteSharkService;
import org.cmdbuild.workflow.xpdl.SharkStyleXpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.ValuePairXpdlExtendedAttributeWidgetFactory;
import org.cmdbuild.workflow.xpdl.XpdlExtendedAttributeVariableFactory;
import org.cmdbuild.workflow.xpdl.XpdlManager;
import org.cmdbuild.workflow.xpdl.XpdlManager.GroupQueryAdapter;
import org.cmdbuild.workflow.xpdl.XpdlProcessDefinitionStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Workflow {

	@Autowired
	@Qualifier("default")
	private AuthenticationService authenticationService;

	@Autowired
	private EmailLogic emailLogic;

	@Autowired
	private FilesStore filesStore;

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private Notifier notifier;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	@Qualifier("system")
	private PrivilegeContext systemPrivilegeContext;

	@Autowired
	private TemplateRepository templateRepository;

	@Autowired
	private UserStore userStore;

	@Autowired
	private WorkflowConfiguration workflowConfiguration;

	@Bean
	public AbstractSharkService workflowService() {
		return new RemoteSharkService(workflowConfiguration);
	}

	@Bean
	public WorkflowLogger workflowLogger() {
		return new WorkflowLogger();
	}

	@Bean
	protected GroupQueryAdapter groupQueryAdapter() {
		return new DefaultGroupQueryAdapter(authenticationService);
	}

	@Bean
	protected XpdlExtendedAttributeVariableFactory xpdlExtendedAttributeVariableFactory() {
		return new SharkStyleXpdlExtendedAttributeVariableFactory();
	}

	@Bean
	protected ValuePairXpdlExtendedAttributeWidgetFactory xpdlExtendedAttributeWidgetFactory() {
		return new DefaultXpdlExtendedAttributeWidgetFactory(templateRepository, notifier, systemDataView, emailLogic);
	}

	@Bean
	protected XpdlProcessDefinitionStore processDefinitionStore() {
		return new XpdlProcessDefinitionStore(workflowService(), xpdlExtendedAttributeVariableFactory(),
				xpdlExtendedAttributeWidgetFactory());
	}

	@Bean
	public ProcessDefinitionManager processDefinitionManager() {
		return new XpdlManager(groupQueryAdapter(), processDefinitionStore());
	}

	@Bean
	public WorkflowTypesConverter workflowTypesConverter() {
		return new SharkTypesConverterBuilder() //
				.withDataView(systemDataView) //
				.withLookupStore(lookupStore) //
				.build();
	}

	@Bean
	@Scope("prototype")
	protected WorkflowPersistence systemWorkflowPersistence() {
		final OperationUser operationUser = userStore.getUser();
		return new DataViewWorkflowPersistenceBuilder() //
				.withPrivilegeContext(systemPrivilegeContext) //
				.withOperationUser(operationUser) // FIXME use system user
				.withDataView(systemDataView) //
				.withProcessDefinitionManager(processDefinitionManager()) //
				.withLookupStore(lookupStore) //
				.withWorkflowService(workflowService()) //
				.build();
	}

	@Bean
	protected WorkflowEventManager workflowEventManager() {
		return new WorkflowEventManagerImpl(systemWorkflowPersistence(), workflowService(), workflowTypesConverter());
	}

	@Bean
	@Scope("prototype")
	@Qualifier("system")
	protected Builder<DefaultWorkflowEngine> systemWorkflowEngineBuilder() {
		final OperationUser operationUser = userStore.getUser();
		return new DefaultWorkflowEngineBuilder() //
				.withOperationUser(operationUser) // FIXME use system user
				.withPersistence(systemWorkflowPersistence()) //
				.withService(workflowService()) //
				.withTypesConverter(workflowTypesConverter()) //
				.withEventListener(workflowLogger()) //
				.withAuthenticationService(authenticationService);
	}

	@Bean
	@Scope("prototype")
	public SystemWorkflowLogicBuilder systemWorkflowLogicBuilder() {
		return new SystemWorkflowLogicBuilder( //
				systemPrivilegeContext, //
				systemWorkflowEngineBuilder(), //
				systemDataView, //
				systemDataView, //
				lookupStore, //
				workflowConfiguration, //
				filesStore);
	}

	@Bean
	protected UpdateOperationListener updateOperationListener() {
		return new UpdateOperationListenerImpl(workflowService(), workflowEventManager());
	}

}
