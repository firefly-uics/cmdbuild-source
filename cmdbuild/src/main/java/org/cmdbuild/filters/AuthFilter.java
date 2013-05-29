package org.cmdbuild.filters;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

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
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.AuthenticationLogic.ClientAuthenticationRequest;
import org.cmdbuild.logic.auth.AuthenticationLogic.ClientAuthenticationResponse;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class AuthFilter implements Filter {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(AuthFilter.class.getName());

	private static class ClientRequestWrapper implements ClientAuthenticationRequest {

		private static class ClientRequestWrapperBuilder implements Builder<ClientRequestWrapper> {

			private HttpServletRequest request;
			private UserStore userStore;

			private ClientRequestWrapperBuilder() {
				// prevents instantiation
			}

			@Override
			public ClientRequestWrapper build() {
				return new ClientRequestWrapper(this);
			}

			public ClientRequestWrapperBuilder withRequest(final HttpServletRequest request) {
				this.request = request;
				return this;
			}

			public ClientRequestWrapperBuilder withUserStore(final UserStore userStore) {
				this.userStore = userStore;
				return this;
			}

		}

		public static ClientRequestWrapperBuilder newInstance() {
			return new ClientRequestWrapperBuilder();
		}

		private final HttpServletRequest request;
		private final UserStore userStore;

		private ClientRequestWrapper(final ClientRequestWrapperBuilder builder) {
			this.request = builder.request;
			this.userStore = builder.userStore;
		}

		@Override
		public String getRequestUrl() {
			return request.getRequestURL().toString();
		}

		@Override
		public String getHeader(final String name) {
			return request.getHeader(name);
		}

		@Override
		public String getParameter(final String name) {
			return request.getParameter(name);
		}

		@Override
		public UserStore getUserStore() {
			return userStore;
		}

	}

	public static final String LOGIN_URL = "index.jsp";

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
			}
			final UserStore userStore = applicationContext().getBean(UserStore.class);
			OperationUser user = userStore.getUser();
			if (!user.isValid()) {
				logger.debug(marker, "user is not valid, trying login using HTTP request");
				final AuthenticationLogic authenticationLogic = applicationContext().getBean("authLogic",
						AuthenticationLogic.class);
				final ClientAuthenticationResponse clientAuthenticatorResponse = authenticationLogic
						.login(ClientRequestWrapper.newInstance() //
								.withRequest(httpRequest) //
								.withUserStore(userStore) //
								.build());
				user = userStore.getUser();
				final String authenticationRedirectUrl = clientAuthenticatorResponse.getRedirectUrl();
				if (clientAuthenticatorResponse.getRedirectUrl() != null) {
					redirectToCustom(authenticationRedirectUrl);
				}
			}
			if (user.isValid()) {
				logger.debug(marker, "user is valid");
				if (isLoginPage(uri)) {
					logger.debug(marker, "redirecting to management");
					redirectToManagement(httpResponse);
				}
			} else {
				logger.debug(marker, "user is not valid");
				if (isProtectedPage(uri)) {
					logger.debug(marker, "'{}' requires authentication, redirecting to login", uri);
					redirectToLogin(httpResponse);
				}
			}
			filterChain.doFilter(request, response);
		} catch (final RedirectException re) {
			re.sendRedirect(httpResponse);
		}
	}

	private void redirectToManagement(final HttpServletResponse response) throws IOException, RedirectException {
		throw new RedirectException("management.jsp");
	}

	private void redirectToLogin(final HttpServletResponse response) throws IOException, RedirectException {
		throw new RedirectException(LOGIN_URL);
	}

	private void redirectToCustom(final String uri) throws IOException, RedirectException {
		throw new RedirectException(uri);
	}

	private boolean isRootPage(final String uri) {
		return uri.equals("/");
	}

	private boolean isLoginPage(final String uri) {
		return uri.equals("/" + LOGIN_URL);
	}

	protected boolean isProtectedPage(final String uri) {
		final boolean isException = uri.startsWith("/services/") || uri.startsWith("/shark/")
				|| uri.matches("^(.*)(css|js|png|jpg|gif)$") || isLoginPage(uri);
		return !isException;
	}
}
