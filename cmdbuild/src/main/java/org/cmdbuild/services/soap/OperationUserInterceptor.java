package org.cmdbuild.services.soap;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.UserTypeStore;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OperationUserInterceptor extends AbstractPhaseInterceptor<Message> implements ApplicationContextAware {

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserTypeStore userTypeStore;

	private AuthenticationLogic authenticationLogic;

	public OperationUserInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		authenticationLogic = applicationContext.getBean("soapAuthenticationLogic", AuthenticationLogic.class);
	}

	@Override
	public void handleMessage(final Message message) throws org.apache.cxf.interceptor.Fault {
		final String authData = new WebserviceUtils().getAuthData(message);
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
	};

	private LoginDTO loginFor(final LoginAndGroup loginAndGroup) {
		return LoginDTO.newInstance() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build();
	}

}
