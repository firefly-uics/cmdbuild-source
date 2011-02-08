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

import org.cmdbuild.exception.RedirectException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.auth.UserContext;

public class AuthFilter implements Filter {

	public static final String LOGIN_URL = "index.jsp";

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		try {
			String uri = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
			if (isRootPage(uri)) {
				redirectToLogin(httpResponse);
			} else if (isLoginPage(uri)) {
				if (AuthenticationFacade.isLoggedIn(httpRequest)) {
					UserContext userCtx = new SessionVars().getCurrentUserContext();
					if (userCtx.hasDefaultGroup()) {
						redirectToManagement(httpResponse);
					}
				}
			} else if (isApplicable(uri)) {
				if (AuthenticationFacade.isLoggedIn(httpRequest)) {
					UserContext userCtx = new SessionVars().getCurrentUserContext();
					if (!userCtx.hasDefaultGroup() && !isLogout(uri)) {
						redirectToLogin(httpResponse);
					}
				} else {
					redirectToLogin(httpResponse);
				}
			}
			filterChain.doFilter(request, response);
		} catch (RedirectException re) {
			re.sendRedirect(httpResponse);
		}
	}

	private void redirectToManagement(HttpServletResponse response) throws IOException, RedirectException {
		throw new RedirectException("management.jsp");
	}

	private void redirectToLogin(HttpServletResponse response) throws IOException, RedirectException {
		throw new RedirectException(LOGIN_URL);
	}

	private boolean isLogout(String uri) {
		return uri.equals("/logout.jsp");
	}

	private boolean isRootPage(String uri) {
		return uri.equals("/");
	}

	private boolean isLoginPage(String uri) {
		return uri.equals("/"+LOGIN_URL);
	}

    protected boolean isApplicable(String uri){
		boolean isException = uri.startsWith("/services/") ||
			uri.startsWith("/shark/") ||
			uri.startsWith("/cmdbuildrest/") ||
			uri.matches("^(.*)(css|js|png|jpg|gif)$");
		return !isException;
    }
}
