package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.CMDataView;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultAuthenticationLogicBuilder extends AuthenticationLogicBuilder {

	@Autowired
	public DefaultAuthenticationLogicBuilder( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView, //
			final UserStore userStore //
	) {
		super(authenticationService, privilegeContextFactory, dataView, userStore);
	}

}
