package org.cmdbuild.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringIntegrationUtils {

	private static final ApplicationContext applicationContext = new ClassPathXmlApplicationContext(
			"application-context.xml");

	private SpringIntegrationUtils() {
		// prevents instantiation
	}

	public static ApplicationContext applicationContext() {
		return applicationContext;
	}

}
