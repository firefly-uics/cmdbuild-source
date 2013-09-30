package org.cmdbuild.services.soap;

import java.util.Collections;

import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.cmdbuild.auth.DefaultAuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.UserTypeStore;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.ForwardingAuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.LoginDTO;
import org.cmdbuild.logic.auth.SoapAuthenticationLogicBuilder;
import org.cmdbuild.services.auth.UserType;
import org.cmdbuild.services.soap.security.LoginAndGroup;
import org.cmdbuild.services.soap.security.PasswordHandler.AuthenticationString;
import org.cmdbuild.services.soap.utils.WebserviceUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class OperationUserInterceptor extends AbstractPhaseInterceptor<Message> implements ApplicationContextAware {

	private static class AuthenticatedUserWithExtendsUsername extends ForwardingAuthenticatedUser {

		public static AuthenticatedUserWithExtendsUsername from(final AuthenticatedUser authenticatedUser) {
			return new AuthenticatedUserWithExtendsUsername(authenticatedUser);
		}

		private static final String SYSTEM = "system";
		private static final String FORMAT = "%s / %s";

		private AuthenticatedUserWithExtendsUsername(final AuthenticatedUser authenticatedUser) {
			super(authenticatedUser);

		}

		@Override
		public String getUsername() {
			return String.format(FORMAT, SYSTEM, super.getUsername());
		}

	}

	private static Iterable<String> EMPTY_PRIVILEGED_SERVICE_USERS = Collections.emptyList();

	@Autowired
	private UserStore userStore;

	@Autowired
	private UserTypeStore userTypeStore;

	@Autowired
	@Qualifier("soap")
	private DefaultAuthenticationService.Configuration configuration;

	private ApplicationContext applicationContext;

	public OperationUserInterceptor() {
		super(Phase.PRE_INVOKE);
	}

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	private AuthenticationLogic authenticationLogic() {
		return applicationContext.getBean(SoapAuthenticationLogicBuilder.class).build();
	}

	@Override
	public void handleMessage(final Message message) throws org.apache.cxf.interceptor.Fault {
		final String authData = new WebserviceUtils().getAuthData(message);
		final AuthenticationString authenticationString = new AuthenticationString(authData);
		storeOperationUser(authenticationString);
	}

	private void storeOperationUser(final AuthenticationString authenticationString) {
		final AuthenticationLogic authenticationLogic = authenticationLogic();
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
		if (authenticationString.shouldImpersonate()) {
			wrapExistingOperationUser(authenticationString);
		}
	}

	private LoginDTO loginFor(final LoginAndGroup loginAndGroup) {
		return LoginDTO.newInstance() //
				.withLoginString(loginAndGroup.getLogin().getValue()) //
				.withGroupName(loginAndGroup.getGroup()) //
				.withNoPasswordRequired() //
				.withUserStore(userStore) //
				.build();
	}

	private Iterable<String> privilegedServiceUsers() {
		return (configuration.getPrivilegedServiceUsers() == null) ? EMPTY_PRIVILEGED_SERVICE_USERS : configuration
				.getPrivilegedServiceUsers();
	};

	private void wrapExistingOperationUser(final AuthenticationString authenticationString) {
		final OperationUser operationUser = userStore.getUser();
		final AuthenticatedUser authenticatedUser = operationUser.getAuthenticatedUser();
		final Iterable<String> privilegedServiceUsers = privilegedServiceUsers();
		final String maybeServiceUser = authenticationString.getAuthenticationLogin().getLogin().getValue();
		for (final String privilegedServiceUser : privilegedServiceUsers) {
			if (privilegedServiceUser.equals(maybeServiceUser)) {
				userStore.setUser(new OperationUser( //
						AuthenticatedUserWithExtendsUsername.from(authenticatedUser), //
						operationUser.getPrivilegeContext(), //
						operationUser.getPreferredGroup()));
			}
		}
	}

}
