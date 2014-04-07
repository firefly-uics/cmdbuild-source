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

import org.cmdbuild.services.DefaultPatchManager;
import org.cmdbuild.services.PatchManager;

public class PatchManagerFilter implements Filter {
	private static final String JSP_PAGE = "patchmanager.jsp";

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
			ServletException {

		HttpServletRequest httpRequest = ((HttpServletRequest) request);
		// check if the application is configured
		final PatchManager patchManager = applicationContext().getBean(DefaultPatchManager.class);
		if (isApplicable(httpRequest) && !patchManager.isUpdated()) {
			request.getRequestDispatcher(JSP_PAGE).forward(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

	protected boolean isApplicable(HttpServletRequest request) {
		String document = request.getRequestURI();
		boolean isException = document.indexOf("configure") > -1 || document.indexOf("util") > -1
				|| document.matches("^(.*)(css|js|png|jpg|gif)$");
		return !isException;
	}
}
