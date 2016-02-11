package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.auth.LanguageStore;
import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.SetupLogic;
import org.cmdbuild.logic.translation.DefaultSetupFacade;
import org.cmdbuild.logic.translation.SetupFacade;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.cmdbuild.services.setup.PropertiesModulesHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Setup {

	private static final String BIM_MODULE_NAME = "bim";

	@Autowired
	private LanguageStore languageStore;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Bean
	@Scope(PROTOTYPE)
	public SetupLogic setupLogic() {
		return new SetupLogic(privilegedModulesHandler());
	}

	@Bean
	@Scope(PROTOTYPE)
	// TODO: check!
	public SetupFacade setupFacade() {
		return new DefaultSetupFacade(setupLogic(), languageStore);
	}

	@Bean
	@Scope(PROTOTYPE)
	protected PrivilegedModulesHandler privilegedModulesHandler() {
		final PrivilegedModulesHandler privilegedModulesHandler = new PrivilegedModulesHandler(defaultModulesHandler(),
				privilegeManagement.userPrivilegeContext());
		privilegedModulesHandler.skipPrivileges(BIM_MODULE_NAME);
		return privilegedModulesHandler;
	}

	@Bean
	@Scope(PROTOTYPE)
	protected DefaultModulesHandler defaultModulesHandler() {
		return new DefaultModulesHandler(propertiesModulesHandler());
	}

	@Bean
	protected PropertiesModulesHandler propertiesModulesHandler() {
		return new PropertiesModulesHandler();
	}

}
