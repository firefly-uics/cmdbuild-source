package org.cmdbuild.filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.spring.annotations.FilterComponent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Sets the language session parameter if requested with a request parameter or
 * if not already set
 * 
 */

@FilterComponent("TranslationFilter")
public class TranslationFilter implements Filter {

	private static final String LANGUAGE_ARG = "language";

	private final LanguageStore languageStore;

	@Autowired
	public TranslationFilter( //
			final LanguageStore languageStore //
	) {
		this.languageStore = languageStore;
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain filterChain)
			throws IOException, ServletException {
		final String language = request.getParameter(LANGUAGE_ARG);
		if (language != null) {
			languageStore.setLanguage(language);
		}
		filterChain.doFilter(request, response);
	}

}
