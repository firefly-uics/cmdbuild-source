package org.cmdbuild.services.soap;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.Response;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.services.auth.OperationUserWrapper;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.soap.operation.DataAccessLogicHelper;
import org.cmdbuild.services.soap.operation.DmsLogicHelper;
import org.cmdbuild.services.soap.operation.LookupLogicHelper;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

abstract class AbstractWebservice implements ApplicationContextAware {

	@Autowired
	private AuthenticationLogic authenticationLogic;

	@Resource
	private WebServiceContext wsc;

	private ApplicationContext applicationContext;

	protected ApplicationContext applicationContext() {
		return applicationContext;
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private static class InMemoryUserStore implements UserStore {

		private OperationUser user;

		@Override
		public OperationUser getUser() {
			return user;
		}

		@Override
		public void setUser(final OperationUser user) {
			this.user = user;
		}

	}

	protected UserContext userContext() {
		return new OperationUserWrapper(operationUser());
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
		final UserStore userStore = new InMemoryUserStore();
		final Response response = authenticationLogic.login(LoginDTO.newInstanceBuilder() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build());
		return userStore.getUser();
	}

	protected DmsLogicHelper dmsLogicHelper() {
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		return new DmsLogicHelper(operationUser(), dmsLogic);
	}

	protected LookupLogicHelper lookupLogicHelper() {
		return new LookupLogicHelper(TemporaryObjectsBeforeSpringDI.getLookupLogic());
	}

	protected WorkflowLogicHelper workflowLogicHelper() {
		return new WorkflowLogicHelper(userContext());
	}

	protected DataAccessLogicHelper dataAccessLogicHelper() {
		final CMDataView dataView = TemporaryObjectsBeforeSpringDI.getUserDataView(operationUser());
		final DataAccessLogic datAccessLogic = TemporaryObjectsBeforeSpringDI.getDataAccessLogic(dataView,
				operationUser());
		return new DataAccessLogicHelper(datAccessLogic);
	}

}
