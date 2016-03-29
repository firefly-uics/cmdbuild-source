package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.CMDataView;

public class RestAuthenticationLogicBuilder extends AuthenticationLogicBuilder {

	public RestAuthenticationLogicBuilder( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView //
	) {
		super(authenticationService, privilegeContextFactory, dataView);
	}

}
