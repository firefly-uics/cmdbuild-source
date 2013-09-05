package org.cmdbuild.spring.configuration;

import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.email.EmailAccount;
import org.cmdbuild.data.store.email.EmailAccountStorableConverter;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.setup.DefaultModulesHandler;
import org.cmdbuild.logic.setup.EmailModule;
import org.cmdbuild.logic.setup.SetUpLogic;
import org.cmdbuild.logic.setup.SetUpLogic.Module;
import org.cmdbuild.services.setup.PrivilegedModulesHandler;
import org.cmdbuild.services.setup.PropertiesModulesHandler;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class SetUp {

	@Autowired
	private PrivilegeManagement privilegeManagement;

	@Autowired
	private SchedulerLogic schedulerLogic;

	@Autowired
	private DBDataView systemDataView;

	@Bean
	public SetUpLogic setUpLogic() {
		return new SetUpLogic(privilegedModulesHandler());
	}

	@Bean
	@Scope("prototype")
	protected PrivilegedModulesHandler privilegedModulesHandler() {
		return new PrivilegedModulesHandler(defaultModulesHandler(), privilegeManagement.userPrivilegeContext());
	}

	@Bean
	@Scope("prototype")
	protected DefaultModulesHandler defaultModulesHandler() {
		final DefaultModulesHandler modulesHandler = new DefaultModulesHandler(propertiesModulesHandler());
		modulesHandler.override("email", emailModule());
		return modulesHandler;
	}

	@Bean
	protected Module emailModule() {
		return new EmailModule(emailAccountStore());
	}

	@Bean
	protected Store<EmailAccount> emailAccountStore() {
		return new DataViewStore<EmailAccount>(systemDataView, emailAccountStorableConverter());
	}

	@Bean
	protected StorableConverter<EmailAccount> emailAccountStorableConverter() {
		return new EmailAccountStorableConverter();
	}

	@Bean
	protected PropertiesModulesHandler propertiesModulesHandler() {
		return new PropertiesModulesHandler();
	}

}
