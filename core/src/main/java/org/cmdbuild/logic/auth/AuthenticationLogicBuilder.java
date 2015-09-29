package org.cmdbuild.logic.auth;

import org.apache.commons.lang3.builder.Builder;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.CMDataView;

public abstract class AuthenticationLogicBuilder implements Builder<AuthenticationLogic> {

	private final AuthenticationService authenticationService;
	private final PrivilegeContextFactory privilegeContextFactory;
	private final CMDataView dataView;

	protected AuthenticationLogicBuilder( //
			final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			final CMDataView dataView //
	) {
		this.authenticationService = authenticationService;
		this.privilegeContextFactory = privilegeContextFactory;
		this.dataView = dataView;
	}

	@Override
	public AuthenticationLogic build() {
		return new DefaultAuthenticationLogic(authenticationService, privilegeContextFactory, dataView);
	}

}
