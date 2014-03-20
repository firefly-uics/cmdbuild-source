package org.cmdbuild.auth;

import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Checks password stored in the DAO layer
 */
public class SoapDatabaseAuthenticator extends DatabaseAuthenticator {

	public SoapDatabaseAuthenticator(final CMDataView view) {
		super(view);
	}

	public SoapDatabaseAuthenticator(final CMDataView view, final Base64Digester digester) {
		super(view, digester);
	}
	
	@Override
	protected String loginAttributeName(Login login) {
		// TODO Auto-generated method stub
		return super.loginAttributeName(login);
	}

}
