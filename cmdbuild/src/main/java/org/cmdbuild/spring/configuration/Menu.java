package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.GroupFetcher;
import org.cmdbuild.auth.acl.PrivilegeContextFactory;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.converter.ViewConverter;
import org.cmdbuild.logic.DashboardLogic;
import org.cmdbuild.logic.data.access.SystemDataAccessLogicBuilder;
import org.cmdbuild.logic.data.access.UserDataAccessLogicBuilder;
import org.cmdbuild.logic.view.ViewLogic;
import org.cmdbuild.services.store.menu.DataViewMenuStore;
import org.cmdbuild.services.store.menu.MenuItemConverter;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Menu {

	@Autowired
	private DashboardLogic dashboardLogic;

	@Autowired
	private GroupFetcher groupFetcher;

	@Autowired
	private PrivilegeContextFactory privilegeContextFactory;

	@Autowired
	private SystemDataAccessLogicBuilder systemDataAccessLogicBuilder;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserDataAccessLogicBuilder userDataAccessLogicBuilder;

	@Autowired
	private ViewConverter viewConverter;

	@Autowired
	private ViewLogic viewLogic;

	@Bean
	public MenuItemConverter menuItemConverter() {
		return new MenuItemConverter(systemDataView, systemDataAccessLogicBuilder);
	}

	@Bean
	@Scope("prototype")
	public DataViewMenuStore dataViewMenuStore() {
		return new DataViewMenuStore( //
				systemDataView, //
				groupFetcher, //
				dashboardLogic, //
				userDataAccessLogicBuilder, //
				privilegeContextFactory, //
				viewLogic, //
				menuItemConverter(), //
				viewConverter);
	}

}
