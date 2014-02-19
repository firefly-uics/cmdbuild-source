package org.cmdbuild.services.setup;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.logic.setup.SetUpLogic.Module;
import org.cmdbuild.logic.setup.SetUpLogic.ModulesHandler;

public class PrivilegedModulesHandler implements ModulesHandler {

	private final ModulesHandler modulesHandler;
	private final PrivilegeContext privilegeContext;

	public PrivilegedModulesHandler(final ModulesHandler modulesHandler, final PrivilegeContext privilegeContext) {
		this.modulesHandler = modulesHandler;
		this.privilegeContext = privilegeContext;
	}

	@Override
	public Module get(final String name) {
		final Module module = modulesHandler.get(name);
		return new PrivilegedModule(module, privilegeContext);
	}

}
