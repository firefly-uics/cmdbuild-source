package org.cmdbuild.services.soap;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.operation.DataAccessLogicHelper;
import org.cmdbuild.services.soap.operation.DmsLogicHelper;
import org.cmdbuild.services.soap.operation.LookupLogicHelper;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

abstract class AbstractWebservice implements ApplicationContextAware {

	protected static final List<MetadataGroup> METADATA_NOT_SUPPORTED = Collections.emptyList();

	@Resource
	private WebServiceContext wsc;

	@Autowired
	private UserStore userStore;

	@Autowired
	private AuthenticationLogic authenticationLogic;

	@Autowired
	private LookupLogic lookupLogic;

	@Autowired
	private WorkflowLogic workflowLogic;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	protected UserContext userContext() {
		return new OperationUserWrapper(operationUser(), authenticationLogic);
	}

	protected OperationUser operationUser() {
		final MessageContext msgCtx = wsc.getMessageContext();
		final String authData = new WebserviceUtils().getAuthData(msgCtx);
		final AuthenticationString authenticationString = new AuthenticationString(authData);
		final LoginAndGroup loginAndGroup;
		if (authenticationString.shouldImpersonate()) {
			loginAndGroup = authenticationString.getImpersonationLogin();
		} else {
			loginAndGroup = authenticationString.getAuthenticationLogin();
		}
		final Response response = authenticationLogic.login(LoginDTO.newInstanceBuilder() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build());
		return userStore.getUser();
	}

	protected CMDataView userDataView() {
		return applicationContext.getBean(UserDataView.class);
	}

	protected DmsLogicHelper dmsLogicHelper() {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		return new DmsLogicHelper(operationUser(), dmsLogic);
	}

	protected LookupLogicHelper lookupLogicHelper() {
		return new LookupLogicHelper(lookupLogic);
	}

	protected WorkflowLogicHelper workflowLogicHelper() {
		operationUser();
		return new WorkflowLogicHelper(userContext(), workflowLogic);
	}

	protected DataAccessLogicHelper dataAccessLogicHelper() {
		operationUser();
		return new DataAccessLogicHelper(applicationContext.getBean("userDataAccessLogic", DataAccessLogic.class));
	}

	protected WorkflowEventManager workflowEventManager() {
		return applicationContext.getBean(WorkflowEventManager.class);
	}

}
