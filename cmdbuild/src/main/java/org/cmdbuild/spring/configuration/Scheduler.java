package org.cmdbuild.spring.configuration;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.SchedulerJobConverter;
import org.cmdbuild.logic.scheduler.DefaultJobFactory;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.workflow.SystemWorkflowLogicBuilder;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

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
	public SchedulerLogic schedulerLogic() {
		return new DefaultSchedulerLogic( //
				schedulerJobStore(), //
				schedulerService(), //
				databaseConfiguration, //
				jobFactory());
	}

	@Bean
	protected Store<SchedulerJob> schedulerJobStore() {
		return new DataViewStore<SchedulerJob>(systemDataView, schedulerJobConverter());
	}

	@Bean
	protected StorableConverter<SchedulerJob> schedulerJobConverter() {
		return new SchedulerJobConverter();
	}

	@Bean
	protected JobFactory jobFactory() {
		return new DefaultJobFactory(systemWorkflowLogicBuilder.build());
	}

}
