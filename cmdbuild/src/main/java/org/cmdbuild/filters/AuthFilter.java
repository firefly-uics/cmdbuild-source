package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.services.SessionVars;

public class AuthFilter implements Filter {

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
			final OperationUser user = new SessionVars().getUser();
			if (isRootPage(uri)) {
				redirectToLogin(httpResponse);
			}
			if (user.isValid()) {
				if (isLoginPage(uri)) {
					redirectToManagement(httpResponse);
				}
			} else {
				if (isProtectedPage(uri)) {
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

	private boolean isRootPage(final String uri) {
		return uri.equals("/");
	}

	private boolean isLoginPage(final String uri) {
		return uri.equals("/" + LOGIN_URL);
	}

	protected boolean isProtectedPage(final String uri) {
		final boolean isException = uri.startsWith("/services/") || uri.startsWith("/shark/")
				|| uri.startsWith("/cmdbuildrest/") || uri.matches("^(.*)(css|js|png|jpg|gif)$") || isLoginPage(uri);
		return !isException;
	}
}
