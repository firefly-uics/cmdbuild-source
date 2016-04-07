package org.cmdbuild.filters;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.ClientAuthenticationRequest;
import org.cmdbuild.logic.auth.AuthenticationLogic.ClientAuthenticationResponse;
import org.cmdbuild.logic.auth.StandardAuthenticationLogic;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration("AuthFilter")
@Scope(PROTOTYPE)
public class AuthFilter implements Filter {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(AuthFilter.class.getName());

	private static class ClientRequestWrapper implements ClientAuthenticationRequest {

		private final HttpServletRequest delegate;

		public ClientRequestWrapper(final HttpServletRequest delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getRequestUrl() {
			return delegate.getRequestURL().toString();
		}

		@Override
		public String getHeader(final String name) {
			return delegate.getHeader(name);
		}

		@Override
		public String getParameter(final String name) {
			return delegate.getParameter(name);
		}

	}

	public static final String LOGIN_URL = "index.jsp";
	public static final String LOGOUT_URL = "logout.jsp";

	@Autowired
	private UserStore userStore;

	private AuthenticationLogic authenticationLogic;

	@Autowired
	public void setAuthenticationLogic(final StandardAuthenticationLogic authenticationLogic) {
		this.authenticationLogic = authenticationLogic;
	}

	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			final String uri = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			logger.debug(marker, "request received for '{}'", uri);

			if (isRootPage(uri)) {
				logger.debug(marker, "root page, redirecting to login");
				redirectToLogin(httpResponse);
			} else if (isLoginPage(uri)) {
				if (userStore.getUser().isValid()) {
					redirectToManagement(httpResponse);
				} else {
					logger.debug(marker, "user is not valid, trying login using HTTP request");
					final ClientAuthenticationResponse clientAuthenticatorResponse = doLogin(httpRequest, userStore);
					final String authenticationRedirectUrl = clientAuthenticatorResponse.getRedirectUrl();
					if (authenticationRedirectUrl != null) {
						redirectToCustom(authenticationRedirectUrl);
					} else if (userStore.getUser().isValid()) {
						redirectToManagement(httpResponse);
					}
				}
			} else if (isProtectedPage(uri)) {
				if (!userStore.getUser().isValid()) {
					logger.debug(marker, "user is not valid, trying login using HTTP request");
					final ClientAuthenticationResponse clientAuthenticatorResponse = doLogin(httpRequest, userStore);
					final String authenticationRedirectUrl = clientAuthenticatorResponse.getRedirectUrl();
					if (authenticationRedirectUrl != null) {
						redirectToCustom(authenticationRedirectUrl);
					} else if (!userStore.getUser().isValid() && !isLogoutPage(uri)) {
						redirectToLogin(httpResponse);
					}
				}
			}
			filterChain.doFilter(request, response);
		} catch (final RedirectException re) {
			re.sendRedirect(httpResponse);
		}
	}

	private ClientAuthenticationResponse doLogin(final HttpServletRequest httpRequest, final UserStore userStore) {
		final ClientAuthenticationResponse clientAuthenticatorResponse = authenticationLogic
				.login(new ClientRequestWrapper(httpRequest), userStore);
		return clientAuthenticatorResponse;
	}

	private void redirectToManagement(final HttpServletResponse response) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to management");
		throw new RedirectException("management.jsp");
	}

	private void redirectToLogin(final HttpServletResponse response) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to login");
		throw new RedirectException(LOGIN_URL);
	}

	private void redirectToCustom(final String uri) throws IOException, RedirectException {
		logger.debug(marker, "redirecting to uri '{}'", uri);
		throw new RedirectException(uri);
	}

	private boolean isRootPage(final String uri) {
		return uri.equals("/");
	}

	private boolean isLoginPage(final String uri) {
		return uri.equals("/" + LOGIN_URL);
	}

	private boolean isLogoutPage(final String uri) {
		return uri.equals("/" + LOGOUT_URL);
	}

	protected boolean isProtectedPage(final String uri) {
		final boolean isException = uri.startsWith("/services/") || uri.startsWith("/shark/")
				|| uri.matches("^(.*)(css|js|png|jpg|gif)$") || isLoginPage(uri);
		return !isException;
	}
}
