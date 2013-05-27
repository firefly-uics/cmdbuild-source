package org.cmdbuild.filters;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cmdbuild.auth.LanguageStore;

/**
 * 
 * Sets the language session parameter if requested with a request parameter or
 * if not already set
 * 
 */

public class TranslationFilter implements Filter {

	private static final String LANGUAGE_ARG = "language";

	public void init(FilterConfig arg0) throws ServletException {
	}

	public void destroy() {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException,
			ServletException {
		String language = request.getParameter(LANGUAGE_ARG);
		if (language != null) {
			applicationContext().getBean(LanguageStore.class).setLanguage(language);
		}
		filterChain.doFilter(request, response);
	}
}
