package org.cmdbuild.spring.configuration;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import javax.sql.DataSource;

import org.cmdbuild.auth.UserStore;
import org.cmdbuild.logic.report.DefaultReportLogic;
import org.cmdbuild.logic.report.ReportLogic;
import org.cmdbuild.services.store.report.JDBCReportStore;
import org.cmdbuild.services.store.report.ReportStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class Report {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private UserStore userStore;

	@Bean
	@Scope(PROTOTYPE)
	public ReportLogic reportLogic() {
		return new DefaultReportLogic(reportStore());
	}

	@Bean
	public ReportStore reportStore() {
		return new JDBCReportStore(userStore, dataSource);
	}

}
