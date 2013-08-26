package org.cmdbuild.spring.configuration;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@ConfigurationComponent
public class Scheduler {

	@Autowired
	private DatabaseConfiguration databaseConfiguration;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private SystemWorkflowLogicBuilder systemWorkflowLogicBuilder;

	@Bean
	protected SchedulerExeptionFactory schedulerExeptionFactory() {
		return new DefaultSchedulerExeptionFactory();
	}

	@Bean
	protected SchedulerService schedulerService() {
		return new QuartzSchedulerService(schedulerExeptionFactory());
	}

	@Bean
	@Scope("prototype")
	public SchedulerLogic schedulerLogic() {
		return new DefaultSchedulerLogic( //
				systemDataView, //
				schedulerService(), //
				databaseConfiguration, //
				systemWorkflowLogicBuilder.build());
	}

}
