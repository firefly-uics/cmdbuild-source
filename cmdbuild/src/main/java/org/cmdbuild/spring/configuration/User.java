package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.DEFAULT;
import static org.cmdbuild.spring.util.Constants.PROTOTYPE;
import static org.cmdbuild.spring.util.Constants.SOAP;
import static org.cmdbuild.spring.util.Constants.USER;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.view.PermissiveDataView;
import org.cmdbuild.logger.WorkflowLogger;
import org.cmdbuild.logic.data.access.SoapDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.workflow.UserWorkflowLogicBuilder;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.cmdbuild.workflow.DataViewWorkflowPersistence;
import org.cmdbuild.workflow.DefaultWorkflowEngine;
import org.cmdbuild.workflow.DefaultWorkflowEngine.DefaultWorkflowEngineBuilder;
import org.cmdbuild.workflow.ProcessDefinitionManager;
import org.cmdbuild.workflow.WorkflowPersistence;
import org.cmdbuild.workflow.WorkflowTypesConverter;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class User {

	@Autowired
	@Qualifier(DEFAULT)
	private AuthenticationService authenticationService;

	@Autowired
	private FilesStore filesStore;

	@Autowired
	private LockCard lockCard;

	@Autowired
	private LookupStore lookupStore;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private ProcessDefinitionManager processDefinitionManager;

	@Autowired
	private RowAndColumnPrivilegeFetcher rowAndColumnPrivilegeFetcher;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private SystemUser systemUser;

	@Autowired
	private UserStore userStore;

	@Autowired
	private WorkflowConfiguration workflowConfiguration;

	@Autowired
	private CMWorkflowService workflowService;

	@Autowired
	private WorkflowLogger workflowLogger;

	@Autowired
	private WorkflowTypesConverter workflowTypesConverter;

	@Autowired
	private Workflow workflow;

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(SOAP)
	public SoapDataAccessLogicBuilder soapDataAccessLogicBuilder() {
		return new SoapDataAccessLogicBuilder( //
				systemDataView, //
				lookupStore, //
				permissiveDataView(), //
				userDataView(), //
				userStore.getUser(), //
				lockCard.emptyLockCardManager());
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(USER)
	public UserDataAccessLogicBuilder userDataAccessLogicBuilder() {
		return new UserDataAccessLogicBuilder( //
				systemDataView, //
				lookupStore, //
				permissiveDataView(), //
				userDataView(), //
				userStore.getUser(), //
				lockCard.userLockCardManager());
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(USER)
	public UserDataView userDataView() {
		return new UserDataView( //
				systemDataView, //
				userStore.getUser().getPrivilegeContext(), //
				rowAndColumnPrivilegeFetcher, //
				userStore.getUser());
	}

	@Bean
	@Scope(PROTOTYPE)
	public PermissiveDataView permissiveDataView() {
		return new PermissiveDataView(userDataView(), systemDataView);
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(USER)
	protected Builder<DefaultWorkflowEngine> userWorkflowEngineBuilder() {
		return new DefaultWorkflowEngineBuilder() //
				.withOperationUser(systemUser.operationUserWithSystemPrivileges()) //
				.withPersistence(userWorkflowPersistence()) //
				.withService(workflowService) //
				.withTypesConverter(workflowTypesConverter) //
				.withEventListener(workflowLogger) //
				.withAuthenticationService(authenticationService);
	}

	@Bean
	@Scope(PROTOTYPE)
	protected WorkflowPersistence userWorkflowPersistence() {
		final OperationUser operationUser = userStore.getUser();
		return DataViewWorkflowPersistence.newInstance() //
				.withPrivilegeContext(operationUser.getPrivilegeContext()) //
				.withOperationUser(operationUser) //
				.withDataView(userDataView()) //
				.withProcessDefinitionManager(processDefinitionManager) //
				.withLookupStore(lookupStore) //
				.withWorkflowService(workflowService) //
				.withActivityPerformerTemplateResolverFactory(workflow.activityPerformerTemplateResolverFactory()) //
				.build();
	}

	@Bean
	@Scope(PROTOTYPE)
	@Qualifier(USER)
	public UserWorkflowLogicBuilder userWorkflowLogicBuilder() {
		return new UserWorkflowLogicBuilder( //
				userStore.getUser().getPrivilegeContext(), //
				userWorkflowEngineBuilder(), //
				userDataView(), //
				systemDataView, //
				lookupStore, //
				workflowConfiguration, //
				filesStore);
	}

}
