package org.cmdbuild.services.auth;

import javax.servlet.http.HttpServletRequest;
import org.cmdbuild.auth.ClientRequestAuthenticator.ClientRequest;

/**
 * Wraps HttpServletRequest in a ClientRequest
 */
class ClientRequestWrapper implements ClientRequest {

	final HttpServletRequest request;

	ClientRequestWrapper(final HttpServletRequest request) {
		this.request = request;
	}

	@Override
	public String getRequestUrl() {
		return request.getRequestURL().toString();
	}

	@Override
	public String getHeader(String name) {
		return request.getHeader(name);
	}

	@Override
	public String getParameter(String name) {
		return request.getParameter(name);
	}
}