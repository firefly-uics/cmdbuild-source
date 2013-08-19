package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.PatchManager;
import org.cmdbuild.spring.annotations.FilterComponent;
import org.springframework.beans.factory.annotation.Autowired;

@FilterComponent("PatchManager")
public class PatchManagerFilter implements Filter {

	private static final String JSP_PAGE = "patchmanager.jsp";

	private final PatchManager patchManager;

	@Autowired
	public PatchManagerFilter( //
			final PatchManager patchManager //
	) {
		this.patchManager = patchManager;
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {

		final HttpServletRequest httpRequest = ((HttpServletRequest) request);
		// check if the application is configured
		if (isApplicable(httpRequest) && !patchManager.isUpdated()) {
			request.getRequestDispatcher(JSP_PAGE).forward(request, response);
		} else {
			filterChain.doFilter(request, response);
		}
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
	}

	protected boolean isApplicable(final HttpServletRequest request) {
		final String document = request.getRequestURI();
		final boolean isException = document.indexOf("configure") > -1 || document.indexOf("util") > -1
				|| document.matches("^(.*)(css|js|png|jpg|gif)$");
		return !isException;
	}
}
