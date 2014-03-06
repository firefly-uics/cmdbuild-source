package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.EmailModule;
import org.cmdbuild.logic.setup.SetupLogic;
import org.cmdbuild.logic.setup.SetupLogic.Module;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.cmdbuild.services.setup.PropertiesModulesHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Setup {

	private static final String BIM_MODULE_NAME = "bim";
	private static final String EMAIL_MODULE_NAME = "email";

	@Autowired
	private Email email;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Bean
	@Scope(PROTOTYPE)
	public SetupLogic setupLogic() {
		return new SetupLogic(privilegedModulesHandler());
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
		final DefaultModulesHandler modulesHandler = new DefaultModulesHandler(propertiesModulesHandler());
		modulesHandler.override(EMAIL_MODULE_NAME, emailModule());
		return modulesHandler;
	}

	@Bean
	protected Module emailModule() {
		return new EmailModule(email.emailAccountStore());
	}

	@Bean
	protected PropertiesModulesHandler propertiesModulesHandler() {
		return new PropertiesModulesHandler();
	}

}
