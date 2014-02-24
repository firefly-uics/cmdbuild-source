package unit.logic.taskmanager;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.collect.Maps;

public class DefaultTaskManagerLogicTest {

	private Store<SchedulerJob> store;
	private JobFactory jobFactory;
	private SchedulerService schedulerService;
	private DefaultTaskManagerLogic taskManagerLogic;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		store = mock(Store.class);
		jobFactory = mock(JobFactory.class);
		schedulerService = mock(SchedulerService.class);
		taskManagerLogic = new DefaultTaskManagerLogic(store, schedulerService, jobFactory);
	}

	@Test
	public void startWorkflowTaskAdded() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();
		values.put("foo", "bar");
		values.put("bar", "baz");
		values.put("baz", "foo");
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withName("foo") //
				.withDescription("the description") //
				.withActiveStatus(true) //
				.withProcessClass("Dummy") //
				.withCronExpression("cron expression") //
				.withParameters(values) //
				.build();
		final Storable created = mock(Storable.class);
		when(created.getIdentifier()) //
				.thenReturn("42");
		when(store.create(any(SchedulerJob.class))) //
				.thenReturn(created);
		final SchedulerJob readed = new SchedulerJob(42L) {
			{
				setCode("foo");
				setDescription("the description");
				setDetail("Dummy");
				setCronExpression("cron expression");
				setLegacyParameters(values);
			}
		};
		when(store.read(created)) //
				.thenReturn(readed);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(any(SchedulerJob.class))) //
				.thenReturn(job);

		// when
		taskManagerLogic.add(task);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).create(schedulerJobCaptor.capture());
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForStore = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForStore.getCode(), equalTo("foo"));
		assertThat(capturedForStore.getDescription(), equalTo("the description"));
		assertThat(capturedForStore.getDetail(), equalTo("Dummy"));
		assertThat(capturedForStore.getLegacyParameters(), equalTo(values));

		final SchedulerJob capturedForJobFactory = schedulerJobCaptor.getAllValues().get(1);
		assertThat(capturedForJobFactory.getCode(), equalTo("foo"));
		assertThat(capturedForJobFactory.getDescription(), equalTo("the description"));
		assertThat(capturedForJobFactory.getDetail(), equalTo("Dummy"));
		assertThat(capturedForJobFactory.getLegacyParameters(), equalTo(values));

		final Job capturedJob = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at("cron expression")));
	}
}
