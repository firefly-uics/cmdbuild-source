package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;

import org.apache.ws.security.WSPasswordCallback;
import org.cmdbuild.config.AuthProperties;
import org.cmdbuild.exception.AuthException.AuthExceptionType;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;
import org.jasig.cas.client.util.CommonUtils;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;

public class CasAuthenticator implements Authenticator {

	private final String SKIP_SSO_PARAM = "skipsso";

	private final boolean CAS_RENEW = false;
	private final boolean CAS_GATEWAY = false;

	public CasAuthenticator() {
		if (!AuthProperties.getInstance().isCasConfigured()) {
			throw AuthExceptionType.AUTH_NOT_CONFIGURED.createException();
		}
	}

	@Override
	public UserContext headerAuth(final HttpServletRequest request) throws RedirectException {
		UserContext userCtx = null;

		final String username = getUsernameForCasToken(request);
		if (username != null) {
			userCtx = loginWithUsername(username);
		} else if (hasNoSkipSsoParam(request)) {
			redirectToCasLogin(request);
		}

		return userCtx;
	}

	private UserContext loginWithUsername(final String username) {
		try {
			return new AuthInfo(username).systemAuth();
		} catch (Throwable e) {
			Log.AUTH.warn(String.format("CAS user %s has no valid CMDBuild", username));
			return null;
		}
	}

	private void redirectToCasLogin(final HttpServletRequest request) throws RedirectException {
		final String serviceUrl = getSkipSsoLoginPath(request);
		final String casLoginUrl = CommonUtils.constructRedirectUrl(getCasLoginUrl(), getCasServiceParam(), serviceUrl,
				CAS_RENEW, CAS_GATEWAY);
		throw new RedirectException(casLoginUrl);
	}

	private String getSkipSsoLoginPath(HttpServletRequest request) {
		return String.format("%s?%s", request.getRequestURL(), SKIP_SSO_PARAM);
	}

	private boolean hasNoSkipSsoParam(HttpServletRequest request) {
		return request.getParameter(SKIP_SSO_PARAM) == null;
	}

	private String getUsernameForCasToken(HttpServletRequest request) {
		final String casTicket = request.getParameter(getCasTicketParam());
		String username = null;
		if (casTicket != null) {
			try {
				final TicketValidator ticketValidator = new Cas20ServiceTicketValidator(getCasServerUrl());
				final Assertion assertion = ticketValidator.validate(casTicket, getSkipSsoLoginPath(request));
				username = assertion.getPrincipal().getName();
				Log.AUTH.info("Valid CAS ticket for user " + username);
			} catch (Exception e) {
				Log.AUTH.warn("Could not validate CAS ticket: " + e.getMessage());
			}
		}
		return username;
	}

	private String getCasLoginUrl() {
		return String.format("%s%s", getCasServerUrl(), getCasLoginPage());
	}

	private String getCasServerUrl() {
		return AuthProperties.getInstance().getCasServerUrl();
	}

	private Object getCasLoginPage() {
		return AuthProperties.getInstance().getCasLoginPage();
	}

	private String getCasTicketParam() {
		return AuthProperties.getInstance().getCasTicketParam();
	}

	private String getCasServiceParam() {
		return AuthProperties.getInstance().getCasServiceParam();
	}

	public UserContext jsonRpcAuth(String username, String unencryptedPassword) {
		return null;
	}

	@Override
	public boolean wsAuth(WSPasswordCallback pwcb) {
		return false;
	}

	@Override
	public boolean canChangePassword() {
		return false;
	}

	@Override
	public void changePassword(String username, String oldPassword, String newPassword) {
		throw AuthExceptionType.AUTH_NOT_AUTHORIZED.createException();
	}

	@Override
	public boolean allowsPasswordLogin() {
		return false;
	}
}
