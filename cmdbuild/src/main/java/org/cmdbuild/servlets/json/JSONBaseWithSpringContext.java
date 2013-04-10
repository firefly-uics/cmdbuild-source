package org.cmdbuild.servlets.json;

import static org.cmdbuild.spring.SpringIntegrationUtils.applicationContext;

import org.springframework.context.ApplicationContext;

public class JSONBaseWithSpringContext extends JSONBase {

	protected static ApplicationContext applicationContext = applicationContext();

}
