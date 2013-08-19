package org.cmdbuild.logic.auth;

import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.spring.annotations.LogicComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

@LogicComponent
@Qualifier("soap")
public class SoapAuthenticationLogicBuilder extends AuthenticationLogicBuilder {

	@Autowired
	public SoapAuthenticationLogicBuilder( //
			@Qualifier("soap") final AuthenticationService authenticationService, //
			final PrivilegeContextFactory privilegeContextFactory, //
			@Qualifier("system") final CMDataView dataView, //
			final UserStore userStore //
	) {
		super(authenticationService, privilegeContextFactory, dataView, userStore);
	}

}
