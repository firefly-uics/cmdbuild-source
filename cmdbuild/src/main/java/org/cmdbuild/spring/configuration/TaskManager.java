package org.cmdbuild.spring.configuration;

import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.dao.view.DBDataView;
import org.cmdbuild.data.store.DataViewStore;
import org.cmdbuild.data.store.DataViewStore.StorableConverter;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.DefaultTaskStore;
import org.cmdbuild.data.store.task.TaskDefinition;
import org.cmdbuild.data.store.task.TaskDefinitionConverter;
import org.cmdbuild.data.store.task.TaskParameter;
import org.cmdbuild.data.store.task.TaskParameterConverter;
import org.cmdbuild.data.store.task.TaskStore;
import org.cmdbuild.dms.DmsConfiguration;
import org.cmdbuild.logic.scheduler.DatabaseConfigurationAwareSchedulerLogic;
import org.cmdbuild.logic.scheduler.DefaultSchedulerLogic;
import org.cmdbuild.logic.scheduler.SchedulerLogic;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.LogicAndSchedulerConverter;
import org.cmdbuild.logic.taskmanager.LogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ReadEmailTaskJobFactory;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTaskJobFactory;
import org.cmdbuild.logic.taskmanager.TaskManagerLogic;
import org.cmdbuild.logic.taskmanager.TransactionalTaskManagerLogic;
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
		return new TransactionalTaskManagerLogic(new DefaultTaskManagerLogic(taskConverter(), defaultTaskStore(),
				defaultSchedulerTaskFacade()));
	}

	@Bean
	protected SchedulerFacade defaultSchedulerTaskFacade() {
		return new DefaultSchedulerFacade(quartzSchedulerService(), defaultJobFactory());
	}

	@Bean
	protected LogicAndStoreConverter taskConverter() {
		return new DefaultLogicAndStoreConverter();
	}

	@Bean
	protected SchedulerExeptionFactory schedulerExeptionFactory() {
		return new DefaultSchedulerExeptionFactory();
	}

	@Bean
	protected SchedulerService quartzSchedulerService() {
		return new QuartzSchedulerService(schedulerExeptionFactory());
	}

	@Bean
	public SchedulerLogic databaseConfigurationAwareSchedulerLogic() {
		return new DatabaseConfigurationAwareSchedulerLogic( //
				defaultSchedulerLogic(), //
				databaseConfiguration //
		);
	}

	private SchedulerLogic defaultSchedulerLogic() {
		return new DefaultSchedulerLogic(defaultTaskStore(), quartzSchedulerService());
	}

	@Bean
	protected TaskStore defaultTaskStore() {
		return new DefaultTaskStore(dataViewSchedulerJobStore(), dataViewSchedulerJobParameterStore());
	}

	@Bean
	protected Store<TaskDefinition> dataViewSchedulerJobStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobConverter());
	}

	@Bean
	protected StorableConverter<TaskDefinition> schedulerJobConverter() {
		return new TaskDefinitionConverter();
	}

	@Bean
	protected Store<TaskParameter> dataViewSchedulerJobParameterStore() {
		return DataViewStore.newInstance(systemDataView, schedulerJobParameterStoreConverter());
	}

	@Bean
	protected StorableConverter<TaskParameter> schedulerJobParameterStoreConverter() {
		return new TaskParameterConverter();
	}

	@Bean
	protected LogicAndSchedulerConverter defaultJobFactory() {
		final DefaultLogicAndSchedulerConverter converter = new DefaultLogicAndSchedulerConverter();
		converter.register(ReadEmailTask.class, readEmailTaskJobFactory());
		converter.register(StartWorkflowTask.class, startWorkflowTaskJobFactory());
		return converter;
	}

	@Bean
	protected ReadEmailTaskJobFactory readEmailTaskJobFactory() {
		return new ReadEmailTaskJobFactory( //
				email.emailAccountStore(), //
				configurableEmailServiceFactory, //
				emailReceiving.answerToExistingFactory(), //
				emailReceiving.downloadAttachmentsFactory(), //
				emailReceiving.startWorkflowFactory());
	}

	@Bean
	protected StartWorkflowTaskJobFactory startWorkflowTaskJobFactory() {
		return new StartWorkflowTaskJobFactory(workflow.systemWorkflowLogicBuilder().build());
	}

}
