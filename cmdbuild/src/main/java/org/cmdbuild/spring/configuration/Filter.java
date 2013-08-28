package org.cmdbuild.spring.configuration;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.services.store.DataViewFilterStore;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Filter {

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private UserStore userStore;

	@Bean
	@Scope("prototype")
	public DataViewFilterStore dataViewFilterStore() {
		return new DataViewFilterStore(systemDataView, operationUser());
	}

	@Bean
	@Scope("prototype")
	protected OperationUser operationUser() {
		return userStore.getUser();
	}

}
