package unit.logic.taskmanager;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.logic.taskmanager.DefaultScheduledTaskConverterFactory;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DefaultScheduledTaskConverterFactoryTest {

	private DefaultScheduledTaskConverterFactory converterFactory;

	@Before
	public void setUp() throws Exception {
		converterFactory = new DefaultScheduledTaskConverterFactory();
	}

	@Test
	public void startWorkflowTaskSuccessfullyConverter() throws Exception {
		// given
		final Map<String, String> parameters = Maps.newHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo\nlol");
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("the description") //
				.withActiveStatus(true) //
				.withCronExpression("the cron expression") //
				.withProcessClass("the class name") //
				.withParameters(parameters) //
				.build();

		// when
		final SchedulerJob converted = converterFactory.of(task).toSchedulerJob();

		// then
		assertThat(converted, instanceOf(WorkflowSchedulerJob.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("the description"));
		assertThat(converted.getCronExpression(), equalTo("the cron expression"));
		assertThat(converted.isRunning(), equalTo(true));
		final WorkflowSchedulerJob _converted = WorkflowSchedulerJob.class.cast(converted);
		assertThat(_converted.getProcessClass(), equalTo("the class name"));
		assertThat(_converted.getParameters(), equalTo(parameters));
	}

	@Test
	public void workflowSchedulerJobSuccessfullyConverter() throws Exception {
		// given
		final Map<String, String> parameters = Maps.newHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo\nlol");
		final WorkflowSchedulerJob schedulerJob = new WorkflowSchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
				setCronExpression("the cron expression");
				setProcessClass("the class name");
				setParameters(parameters);
			}
		};

		// when
		final ScheduledTask converted = converterFactory.of(schedulerJob).toScheduledTask();

		// then
		assertThat(converted, instanceOf(StartWorkflowTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("the description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("the cron expression"));
		final StartWorkflowTask convertedStartWorkflowTask = StartWorkflowTask.class.cast(converted);
		assertThat(convertedStartWorkflowTask.getProcessClass(), equalTo("the class name"));
		assertThat(convertedStartWorkflowTask.getParameters(), equalTo(parameters));
	}

}
