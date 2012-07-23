package org.cmdbuild.servlets.resource;

import javax.servlet.http.HttpServletRequest;


public interface BasicProtectedResource extends Resource {

	boolean checkAuthentication(String user, String password, HttpServletRequest request);
	String authRealm();
}
