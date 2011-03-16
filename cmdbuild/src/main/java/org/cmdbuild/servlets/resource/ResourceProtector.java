package org.cmdbuild.servlets.resource;

import javax.servlet.http.HttpServletRequest;

public interface ResourceProtector {
	boolean validateRequest(HttpServletRequest req);
}
