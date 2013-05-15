package org.cmdbuild.logic.auth;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;

public class AuthenticationLogicUtils {

	private AuthenticationLogicUtils() {
		// prevents instantiation
	}

	// FIXME: method maybe not implemented correctly (headerAuth? autoLogin?)
	public static boolean isLoggedIn(final HttpServletRequest request) throws RedirectException {

		final OperationUser operationUser = applicationContext().getBean(UserStore.class).getUser();
		if (operationUser == null) {
			return false;
		}
		if (operationUser.getAuthenticatedUser().isAnonymous()) {
			return false;
		}

		// TODO: see in the history what did headerAuth method do....

		// UserContext userCtx = new SessionVars().getCurrentUserContext();
		// if (userCtx == null) {
		// final AuthenticationService as = new DefaultAuthenticationService();
		// userCtx = as.headerAuth(request);
		// if (userCtx != null) {
		// new SessionVars().setCurrentUserContext(userCtx);
		// }
		// }
		// return (userCtx != null || doAutoLogin());
		// return new SessionVars().getUser().isValid(); // isAnonymous +
		// doAutoLogin
		return operationUser.isValid();
	}

	public static void assureAdmin(final HttpServletRequest request, final AdminAccess adminAccess) {
		// TODO: manage DemoMode... see history from thg
		final OperationUser operationUser = applicationContext().getBean(UserStore.class).getUser();
		if (operationUser == null
				|| (!operationUser.hasAdministratorPrivileges() && !operationUser.hasDatabaseDesignerPrivileges())) {
			throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
		}
	}

	// TODO
	private static boolean doAutoLogin() {
		// try {
		// final String username = AuthProperties.getInstance().getAutologin();
		// if (username != null && username.length() > 0) {
		// loginAs(username);
		// Log.OTHER.info("Autologin with user " + username);
		// return true;
		// }
		// } catch (final Exception e) {
		// Log.OTHER.warn("Autologin failed");
		// }
		return false;
	}

}
