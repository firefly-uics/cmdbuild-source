package org.cmdbuild.spring.configuration;

import org.cmdbuild.dao.driver.DBDriver;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class Base {

	@Autowired
	private DBDriver dbDriver;

	@Bean
	@Qualifier("system")
	public DBDataView systemDataView() {
		return new DBDataView(dbDriver);
	}

}
