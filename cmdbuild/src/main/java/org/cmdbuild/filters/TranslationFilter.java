package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cmdbuild.services.SessionVars;

/**
 * 
 * Sets the language session parameter if requested with a request
 * parameter or if not already set
 *
 */

public class TranslationFilter implements Filter {

	private static final String LANGUAGE_ARG = "language";

	public void init(FilterConfig arg0) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		String language = request.getParameter(LANGUAGE_ARG);
		if (language != null) {
			new SessionVars().setLanguage(language);
		}
        filterChain.doFilter(request, response);
	}
}
