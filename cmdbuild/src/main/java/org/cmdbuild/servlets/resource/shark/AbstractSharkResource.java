package org.cmdbuild.servlets.resource.shark;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.cmdbuild.services.WorkflowService;
import org.cmdbuild.servlets.resource.AbstractResource;
import org.cmdbuild.servlets.resource.BasicProtectedResource;

public abstract class AbstractSharkResource extends AbstractResource implements
		BasicProtectedResource {
	
	String user;
	String password;

	public boolean checkAuthentication(String user, String password, HttpServletRequest request) {
		return this.user.equals(user) && this.password.equals(password);
	}
	public String authRealm() {
		return "shark";
	}

	public void init(ServletContext ctxt, ServletConfig config) {
		user = config.getInitParameter("httpauth-user");
		password  = config.getInitParameter("httpauth-pwd");

		WorkflowService.getInstance().setBase64Authentication(user, password);
	}

}
