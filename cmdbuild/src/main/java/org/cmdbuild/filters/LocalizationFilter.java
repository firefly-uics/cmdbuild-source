package org.cmdbuild.filters;

import static org.apache.commons.lang3.BooleanUtils.toBoolean;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.cmdbuild.services.localization.RequestHandler;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Sets the localization parameter for the current request.
 */
@Configuration("LocalizationFilter")
public class LocalizationFilter implements Filter, ApplicationContextAware {

	private static final String PARAMETER = "localized";

	private RequestHandler requestHandler;

	@Override
	public void setApplicationContext(final ApplicationContext applicationContext) throws BeansException {
		this.requestHandler = applicationContext.getBean(RequestHandler.class);
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
		// TODO log
		final String value = request.getParameter(PARAMETER);
		//final String value = true;
		requestHandler.setLocalized(toBoolean(value));
		filterChain.doFilter(request, response);
	}

}
