package org.cmdbuild.services.soap;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.UserTypeStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.config.CmdbuildConfiguration;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dao.view.user.UserDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.dms.MetadataGroup;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.logic.WorkflowLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.data.access.DataAccessLogic;
import org.cmdbuild.logic.data.lookup.LookupLogic;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.soap.operation.AuthenticationLogicHelper;
import org.cmdbuild.services.soap.operation.DataAccessLogicHelper;
import org.cmdbuild.services.soap.operation.DmsLogicHelper;
import org.cmdbuild.services.soap.operation.LookupLogicHelper;
import org.cmdbuild.services.soap.operation.WorkflowLogicHelper;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.cmdbuild.services.store.menu.MenuStore;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

abstract class AbstractWebservice implements ApplicationContextAware {

	protected static final Logger logger = Log.SOAP;

	protected static final List<MetadataGroup> METADATA_NOT_SUPPORTED = Collections.emptyList();

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserTypeStore userTypeStore;

	private AuthenticationLogic authenticationLogic;

	@Autowired
	private LookupLogic lookupLogic;

	@Autowired
	private WorkflowLogic workflowLogic;

	@Autowired
	private CmdbuildConfiguration configuration;

	@Resource
	private WebServiceContext wsc;

	private ApplicationContext applicationContext;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		authenticationLogic = applicationContext.getBean("soapAuthenticationLogic", AuthenticationLogic.class);
	}

	// TODO use an interceptor for do this
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
		try {
			userTypeStore.setType(UserType.APPLICATION);
			authenticationLogic.login(loginFor(loginAndGroup));
		} catch (final RuntimeException e) {
			if (authenticationString.shouldImpersonate()) {
				/*
				 * fallback to the autentication login, should always work
				 */
				authenticationLogic.login(loginFor(authenticationString.getAuthenticationLogin()));
				userTypeStore.setType(UserType.GUEST);
			} else {
				throw e;
			}
		}
		return userStore.getUser();
	}

	private LoginDTO loginFor(final LoginAndGroup loginAndGroup) {
		return LoginDTO.newInstanceBuilder() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build();
	}

	protected CMDataView userDataView() {
		operationUser();
		return applicationContext.getBean(UserDataView.class);
	}

	protected DmsLogicHelper dmsLogicHelper() {
		final OperationUser operationUser = operationUser();
		final DmsLogic dmsLogic = applicationContext.getBean(DmsLogic.class);
		return new DmsLogicHelper(operationUser, dmsLogic);
	}

	protected LookupLogicHelper lookupLogicHelper() {
		return new LookupLogicHelper(lookupLogic);
	}

	protected WorkflowLogicHelper workflowLogicHelper() {
		operationUser();
		return new WorkflowLogicHelper(workflowLogic, applicationContext.getBean(UserDataView.class));
	}

	protected DataAccessLogicHelper dataAccessLogicHelper() {
		operationUser();
		final DataAccessLogicHelper helper = new DataAccessLogicHelper( //
				applicationContext.getBean(UserDataView.class),//
				applicationContext.getBean("soapDataAccessLogic", DataAccessLogic.class), //
				applicationContext.getBean("workflowLogic", WorkflowLogic.class), //
				applicationContext.getBean("operationUser", OperationUser.class), //
				applicationContext.getBean(DataSource.class), //
				userTypeStore, //
				configuration);
		helper.setMenuStore(menuStore());
		return helper;
	}

	protected WorkflowEventManager workflowEventManager() {
		return applicationContext.getBean(WorkflowEventManager.class);
	}

	protected DataAccessLogic userDataAccessLogic() {
		return applicationContext.getBean("userDataAccessLogic", DataAccessLogic.class);
	}

	protected LookupStore lookupStore() {
		return applicationContext.getBean("lookupStore", LookupStore.class);
	}

	protected AuthenticationLogicHelper authenticationLogicHelper() {
		final OperationUser operationUser = operationUser();
		final CMDataView dataView = applicationContext.getBean(DBDataView.class);
		return new AuthenticationLogicHelper(operationUser, dataView, userTypeStore);
	}

	protected MenuStore menuStore() {
		return applicationContext().getBean(MenuStore.class);
	}

}
