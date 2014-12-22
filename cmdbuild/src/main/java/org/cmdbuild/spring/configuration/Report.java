package org.cmdbuild.spring.configuration;

import javax.sql.DataSource;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Report {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserStore userStore;

	@Bean
	public ReportStore reportStore() {
		return new JDBCReportStore(userStore, dataSource);
	}

}
