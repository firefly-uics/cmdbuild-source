package org.cmdbuild.connector.ws;

import org.apache.ws.security.WSConstants;
import org.cmdbuild.services.soap.Private;

public class CMDBuildWebServiceClient extends AbstractWebServiceClient<Private> {

	public CMDBuildWebServiceClient(final String url, final String username, final String password) {
		super(Private.class, url, username, password);
	}

	@Override
	protected String getPasswordType() {
		return WSConstants.PW_DIGEST;
	}

}
