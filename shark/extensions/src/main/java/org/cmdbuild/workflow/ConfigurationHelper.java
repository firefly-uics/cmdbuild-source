package org.cmdbuild.workflow;

import static java.lang.String.format;

import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class ConfigurationHelper {

	private static final String CMDBUILD_API_CLASSNAME_PROPERTY = "org.cmdbuild.workflow.api.classname";

	private final CallbackUtilities cus;

	public ConfigurationHelper(final CallbackUtilities cus) {
		this.cus = cus;
	}

	public SharkWorkflowApiFactory getWorkflowApiFactory() throws ClassNotFoundException, InstantiationException,
			IllegalAccessException {
		final String classname = cus.getProperty(CMDBUILD_API_CLASSNAME_PROPERTY);
		cus.info(null, format("loading api '%s'", classname));
		final Class<? extends SharkWorkflowApiFactory> sharkWorkflowApiClass = Class.forName(classname).asSubclass(
				SharkWorkflowApiFactory.class);
		final SharkWorkflowApiFactory sharkWorkflowApi = sharkWorkflowApiClass.newInstance();
		return sharkWorkflowApi;
	}

}
