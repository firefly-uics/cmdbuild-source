package org.cmdbuild.config;

import java.io.IOException;

import org.cmdbuild.services.Settings;
import org.cmdbuild.workflow.service.RemoteSharkService;

public class WorkflowProperties extends DefaultProperties implements RemoteSharkService.Config {

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

	@Override
	public String getServerUrl(){
		return getProperty(ENDPOINT);
	}

	@Override
	public String getUsername(){
		return getProperty(ADMIN_USERNAME);
	}

	@Override
	public String getPassword(){
		return getProperty(ADMIN_PASSWORD);
	}

	@Override
	public void store() throws IOException {
		super.store();
		// TODO it should notify the workflow service
	}
}
