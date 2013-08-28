package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.dms.CachedDmsService;
import org.cmdbuild.dms.DefaultDocumentCreatorFactory;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.dms.DmsService;
import org.cmdbuild.dms.DocumentCreatorFactory;
import org.cmdbuild.dms.LoggedDmsService;
import org.cmdbuild.dms.alfresco.AlfrescoDmsService;
import org.cmdbuild.logic.DmsLogic;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Dms {

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserStore userStore;

	@Bean
	@Qualifier("default")
	public DmsService dmsService() {
		return new CachedDmsService(loggedDmsService());
	}

	@Bean
	protected DmsService loggedDmsService() {
		return new LoggedDmsService(alfrescoDmsService());
	}

	@Bean
	protected DmsService alfrescoDmsService() {
		return new AlfrescoDmsService();
	}

	@Bean
	public DocumentCreatorFactory documentCreatorFactory() {
		return new DefaultDocumentCreatorFactory();
	}

	@Bean
	@Scope("prototype")
	public DmsLogic dmsLogic() {
		return new DmsLogic( //
				dmsService(), //
				userStore.getUser().getPrivilegeContext(), //
				systemDataView, //
				dmsConfiguration, //
				documentCreatorFactory());
	}

}
