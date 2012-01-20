package org.cmdbuild.config;

import java.io.IOException;

import org.cmdbuild.services.Settings;
import org.cmdbuild.services.WorkflowService;

public class WorkflowProperties extends DefaultProperties {

	private static final long serialVersionUID = 8184420208391927123L;
	
	private static final String MODULE_NAME = "workflow";
	
	private static final String ENABLED = "enabled";
	private static final String ENDPOINT = "endpoint";
	private static final String ADMIN_USERNAME = "user";
	private static final String ADMIN_PASSWORD = "password";
	
	public WorkflowProperties() {
		super();
		setProperty(ENABLED, String.valueOf(false));
		setProperty(ENDPOINT, "http://localhost:8080/shark");
		setProperty(ADMIN_USERNAME, "admin");
		setProperty(ADMIN_PASSWORD, "enhydra");
	}

	public static WorkflowProperties getInstance() {
		return (WorkflowProperties)Settings.getInstance().getModule(MODULE_NAME);
	}

	public boolean isEnabled() {
		String enabled = getProperty(ENABLED, Boolean.FALSE.toString());
		return Boolean.parseBoolean(enabled);
	}
	
	public String getEndpoint(){
		return getProperty(ENDPOINT);
	}
	
	public String getUser(){
		return getProperty(ADMIN_USERNAME);
	}
	
	public String getPassword(){
		return getProperty(ADMIN_PASSWORD);
	}

	@Override
	public void store() throws IOException {
		super.store();
		WorkflowService.getInstance().configure();
	}
}
