package org.cmdbuild.shark.toolagent;

import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.cmdbuild.services.soap.Private;

public class ExternalSyncToolAgent extends AbstractWSToolAgent {

	@Override
	protected String getEndpoint() {
		return cus.getProperty(AbstractWSToolAgent.CMDBUILD_ENDPOINT);
	}

	@Override
	protected void invokeWebService(final Private stub, final AppParameter[] params, final String toolInfoID)
			throws Exception {
		final String xml = (String) params[1].the_value;
		params[2].the_value = stub.sync(xml);
	}
}
