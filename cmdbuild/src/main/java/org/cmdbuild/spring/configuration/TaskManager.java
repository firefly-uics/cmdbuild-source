package org.cmdbuild.spring.configuration;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobConverter;
import org.cmdbuild.data.store.scheduler.SchedulerJobParameter;
import org.cmdbuild.data.store.scheduler.SchedulerJobParameterConverter;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.scheduler.DatabaseConfigurationAwareSchedulerLogic;
import org.cmdbuild.logic.scheduler.DefaultJobFactory;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.taskmanager.DefaultScheduledTaskConverterFactory;
import org.cmdbuild.logic.taskmanager.DefaultScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacadeConverterFactory;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.scheduler.SchedulerExeptionFactory;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.quartz.QuartzSchedulerService;
import org.cmdbuild.services.email.ConfigurableEmailServiceFactory;
import org.cmdbuild.services.scheduler.DefaultSchedulerExeptionFactory;
import org.cmdbuild.spring.annotations.ConfigurationComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

@ConfigurationComponent
public class TaskManager {

	@Autowired
	private ConfigurableEmailServiceFactory configurableEmailServiceFactory;

	@Autowired
	private DatabaseConfiguration databaseConfiguration;

	@Autowired
	private Dms dms;

	@Autowired
	private DmsConfiguration dmsConfiguration;

	@Autowired
	private Email email;

	@Autowired
	private EmailReceiving emailReceiving;

	@Autowired
	private DBDataView systemDataView;

	@Autowired
	private Workflow workflow;

	@Bean
	public TaskManagerLogic taskManagerLogic() {
		return new DefaultTaskManagerLogic(schedulerFacade());
	}

	@Bean
	protected ScheduledTaskFacade schedulerFacade() {
		return new DefaultScheduledTaskFacade(scheduledTaskConverterFactory(), advancedSchedulerJobStore(),
				schedulerService(), jobFactory());
	}

	@Bean
	protected ScheduledTaskFacadeConverterFactory scheduledTaskConverterFactory() {
		return new DefaultScheduledTaskConverterFactory();
	}

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
		return new DatabaseConfigurationAwareSchedulerLogic( //
				defaultSchedulerLogic(), //
				databaseConfiguration //
		);
	}

	private SchedulerLogic defaultSchedulerLogic() {
		return new DefaultSchedulerLogic( //
				advancedSchedulerJobStore(), //
				schedulerService(), //
				jobFactory());
	}

	@Bean
	protected Store<SchedulerJob> advancedSchedulerJobStore() {
		return new AdvancedSchedulerJobStore(schedulerJobStore(), schedulerJobParameterStore());
	}

	@Bean
	protected Store<SchedulerJob> schedulerJobStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobConverter());
	}

	@Bean
	protected StorableConverter<SchedulerJob> schedulerJobConverter() {
		return new SchedulerJobConverter();
	}

	@Bean
	protected Store<SchedulerJobParameter> schedulerJobParameterStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobParameterStoreConverter());
	}

	@Bean
	protected StorableConverter<SchedulerJobParameter> schedulerJobParameterStoreConverter() {
		return new SchedulerJobParameterConverter();
	}

	@Bean
	protected JobFactory jobFactory() {
		return new DefaultJobFactory( //
				workflow.systemWorkflowLogicBuilder().build(), //
				email.emailAccountStore(), //
				schedulerJobParameterStore(), //
				configurableEmailServiceFactory, //
				emailReceiving.answerToExistingFactory(), //
				emailReceiving.downloadAttachmentsFactory(), //
				emailReceiving.startWorkflowFactory());
	}

}
