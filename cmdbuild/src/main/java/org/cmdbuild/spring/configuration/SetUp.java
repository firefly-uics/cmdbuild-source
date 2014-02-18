package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.SetUpLogic;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.cmdbuild.services.setup.PropertiesModulesHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class SetUp {

	@Autowired
	private Email email;

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Bean
	@Scope(PROTOTYPE)
	public SetUpLogic setUpLogic() {
		return new SetUpLogic(privilegedModulesHandler());
	}

	@Bean
	@Scope(PROTOTYPE)
	protected PrivilegedModulesHandler privilegedModulesHandler() {
		return new PrivilegedModulesHandler(defaultModulesHandler(), privilegeManagement.userPrivilegeContext());
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
