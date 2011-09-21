package org.cmdbuild.config;

import java.io.IOException;

import org.cmdbuild.services.Settings;
import org.cmdbuild.services.WorkflowService;

public class WorkflowProperties extends DefaultProperties {

	private static final long serialVersionUID = 8184420208391927123L;
	
	private static final String MODULE_NAME = "workflow";
	
	private static final String ENABLED = "enabled";
	private static final String ENDPOINT = "endpoint";//where is the sharkWebServices shark application
	private static final String ADMIN_USERNAME = "user";
	private static final String ADMIN_PASSWORD = "password";
	private static final String ENGINE_NAME = "engine";
	private static final String SCOPE = "scope";
	
	public WorkflowProperties() {
		super();
		setProperty(ENABLED, String.valueOf(false));
		setProperty(ENDPOINT, "http://localhost:8081/sharkWebServices");
		setProperty(ADMIN_USERNAME, "admin");
		setProperty(ADMIN_PASSWORD, "enhydra");
		setProperty(ENGINE_NAME, "shark");
		setProperty(SCOPE, "");
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
	
	public String getEngine(){
		return getProperty(ENGINE_NAME);
	}
	
	public String getScope(){
		return getProperty(SCOPE);
	}

	@Override
	public void store() throws IOException {
		super.store();
		WorkflowService.getInstance().configure();
	}
}
