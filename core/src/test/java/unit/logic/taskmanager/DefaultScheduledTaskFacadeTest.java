package unit.logic.taskmanager;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.RandomStringUtils.randomAscii;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.logic.taskmanager.DefaultScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacadeConverterFactory;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacadeConverterFactory.ScheduledTaskConverter;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacadeConverterFactory.SchedulerJobConverter;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.scheduler.Job;
import org.cmdbuild.scheduler.RecurringTrigger;
import org.cmdbuild.scheduler.SchedulerService;
import org.cmdbuild.scheduler.Trigger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class DefaultScheduledTaskFacadeTest {

	private static final int RANDOM_STRING_SIZE = 10;

	private static final Function<String, String> IDENTITY = identity();
	private static final Map<String, String> PARAMETERS = Maps.uniqueIndex(asList( //
			randomAscii(RANDOM_STRING_SIZE), //
			randomAscii(RANDOM_STRING_SIZE), //
			randomAscii(RANDOM_STRING_SIZE) //
			), IDENTITY);

	private final StartWorkflowTask DUMMY_SCHEDULED_TASK = StartWorkflowTask.newInstance() //
			.withId(42L) //
			.withDescription(randomAscii(RANDOM_STRING_SIZE)) //
			.withActiveStatus(true) //
			.withCronExpression(randomAscii(RANDOM_STRING_SIZE)) //
			.withProcessClass(randomAscii(RANDOM_STRING_SIZE)) //
			.withParameters(PARAMETERS) //
			.build();

	private final Storable STORED = new Storable() {

		@Override
		public String getIdentifier() {
			return randomAscii(RANDOM_STRING_SIZE);
		}

	};

	private final SchedulerJob CONVERTED_RUNNING = new WorkflowSchedulerJob(42L) {

		{
			setDescription(randomAscii(RANDOM_STRING_SIZE));
			setRunning(true);
			setCronExpression(randomAscii(RANDOM_STRING_SIZE));
			setProcessClass(randomAscii(RANDOM_STRING_SIZE));
			setParameters(PARAMETERS);
		}

	};

	private final SchedulerJob CONVERTED_NOT_RUNNING = new WorkflowSchedulerJob(42L) {
		{
			setDescription(randomAscii(RANDOM_STRING_SIZE));
			setRunning(false);
			setCronExpression(randomAscii(RANDOM_STRING_SIZE));
			setProcessClass(randomAscii(RANDOM_STRING_SIZE));
			setParameters(PARAMETERS);
		}
	};

	private final SchedulerJob EXISTING_RUNNING = new WorkflowSchedulerJob(42L) {

		{
			setDescription(randomAscii(RANDOM_STRING_SIZE));
			setRunning(true);
			setCronExpression(randomAscii(RANDOM_STRING_SIZE));
			setProcessClass(randomAscii(RANDOM_STRING_SIZE));
			setParameters(PARAMETERS);
		}

	};

	private final SchedulerJob EXISTING_NOT_RUNNING = new WorkflowSchedulerJob(42L) {
		{
			setDescription(randomAscii(RANDOM_STRING_SIZE));
			setRunning(false);
			setCronExpression(randomAscii(RANDOM_STRING_SIZE));
			setProcessClass(randomAscii(RANDOM_STRING_SIZE));
			setParameters(PARAMETERS);
		}
	};

	private ScheduledTaskFacadeConverterFactory scheduledTaskFacadeConverterFactory;
	private Store<SchedulerJob> store;
	private JobFactory jobFactory;
	private SchedulerService schedulerService;
	private ScheduledTaskFacade schedulerFacade;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		scheduledTaskFacadeConverterFactory = mock(ScheduledTaskFacadeConverterFactory.class);
		store = mock(Store.class);
		jobFactory = mock(JobFactory.class);
		schedulerService = mock(SchedulerService.class);
		schedulerFacade = new DefaultScheduledTaskFacade(scheduledTaskFacadeConverterFactory, store, schedulerService,
				jobFactory);
	}

	@After
	public void verifyNoMoreInteractionsOnMocks() throws Exception {
		verifyNoMoreInteractions(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
	}

	private void mockConversion(final ScheduledTask from, final SchedulerJob to) {
		final ScheduledTaskConverter converter = mock(ScheduledTaskConverter.class);
		when(converter.toSchedulerJob()) //
				.thenReturn(to);
		when(scheduledTaskFacadeConverterFactory.of(from)) //
				.thenReturn(converter);
	}

	private void mockConversion(final SchedulerJob from, final ScheduledTask to) {
		final SchedulerJobConverter converter = mock(SchedulerJobConverter.class);
		when(converter.toScheduledTask()) //
				.thenReturn(to);
		when(scheduledTaskFacadeConverterFactory.of(from)) //
				.thenReturn(converter);
	}

	private void mockCreation(final SchedulerJob newOne, final Storable storedOne) {
		when(store.create(newOne)) //
				.thenReturn(storedOne);
	}

	private void mockRead(final Storable storedOne, final SchedulerJob existingOne) {
		when(store.read(storedOne)) //
				.thenReturn(existingOne);
	}

	private void mockJobFactory(final SchedulerJob from, final Job job) {
		when(jobFactory.create(from)) //
				.thenReturn(job);
	}

	private Job mockJob(final String name) {
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn(name);
		return job;
	}

	@Test
	public void jobPersistedAndStartedWhenCreated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_RUNNING);
		mockCreation(CONVERTED_RUNNING, STORED);
		mockRead(STORED, EXISTING_RUNNING);
		mockJobFactory(EXISTING_RUNNING, mockJob("foo"));

		// when
		schedulerFacade.create(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).create(CONVERTED_RUNNING);
		inOrder.verify(store).read(STORED);
		inOrder.verify(jobFactory).create(EXISTING_RUNNING);
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJob = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(EXISTING_RUNNING.getCronExpression())));
	}

	@Test
	public void jobPersistedAndNotStartedWhenCreated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_NOT_RUNNING);
		mockCreation(CONVERTED_NOT_RUNNING, STORED);
		mockRead(STORED, EXISTING_NOT_RUNNING);

		// when
		schedulerFacade.create(DUMMY_SCHEDULED_TASK);

		// then
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).create(CONVERTED_NOT_RUNNING);
		inOrder.verify(store).read(STORED);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void jobStoppedPersistedAndNotStartedWhenUpdated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_NOT_RUNNING);
		mockRead(CONVERTED_NOT_RUNNING, EXISTING_RUNNING);
		mockJobFactory(EXISTING_RUNNING, mockJob("foo"));

		// when
		schedulerFacade.update(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_NOT_RUNNING);
		inOrder.verify(jobFactory).create(EXISTING_RUNNING);
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getValue();
		assertThat(capturedForUpdate, equalTo(EXISTING_RUNNING));
		assertThat(capturedForUpdate.getDescription(), equalTo(CONVERTED_NOT_RUNNING.getDescription()));
		assertThat(capturedForUpdate.isRunning(), equalTo(CONVERTED_NOT_RUNNING.isRunning()));
		assertThat(capturedForUpdate.getCronExpression(), equalTo(CONVERTED_NOT_RUNNING.getCronExpression()));

		final Job capturedJobForRemove = jobCaptor.getValue();
		assertThat(capturedJobForRemove.getName(), equalTo("foo"));
	}

	@Test
	public void jobStoppedPersistedAndStartedWhenUpdated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_RUNNING);
		mockRead(CONVERTED_RUNNING, EXISTING_RUNNING);
		mockJobFactory(EXISTING_RUNNING, mockJob("foo"));

		// when
		schedulerFacade.update(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_RUNNING);
		inOrder.verify(jobFactory).create(EXISTING_RUNNING);
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verify(store).update(any(SchedulerJob.class));
		inOrder.verify(jobFactory).create(EXISTING_RUNNING);
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJobForRemove = jobCaptor.getAllValues().get(0);
		assertThat(capturedJobForRemove.getName(), equalTo("foo"));

		final Job capturedJobForAdd = jobCaptor.getAllValues().get(1);
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJobForAdd.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(CONVERTED_RUNNING.getCronExpression())));
	}

	@Test
	public void jobNotStoppedPersistedAndNotStartedWhenUpdated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_NOT_RUNNING);
		mockRead(CONVERTED_NOT_RUNNING, EXISTING_NOT_RUNNING);

		// when
		schedulerFacade.update(DUMMY_SCHEDULED_TASK);

		// then
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_NOT_RUNNING);
		inOrder.verify(store).update(EXISTING_NOT_RUNNING);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void jobNotStoppedPersistedAndStartedWhenUpdated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_RUNNING);
		mockRead(CONVERTED_RUNNING, EXISTING_NOT_RUNNING);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(any(SchedulerJob.class))) //
				.thenReturn(job);

		// when
		schedulerFacade.update(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_RUNNING);
		inOrder.verify(store).update(any(SchedulerJob.class));
		inOrder.verify(jobFactory).create(any(SchedulerJob.class));
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJobForAdd = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJobForAdd.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(CONVERTED_RUNNING.getCronExpression())));
	}

	@Test
	public void onlySomeAttributesAreUpdated() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_NOT_RUNNING);
		mockRead(CONVERTED_NOT_RUNNING, EXISTING_NOT_RUNNING);

		// when
		schedulerFacade.update(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_NOT_RUNNING);
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getValue();
		assertThat(capturedForUpdate, equalTo(EXISTING_NOT_RUNNING));
		assertThat(capturedForUpdate.getDescription(), equalTo(CONVERTED_NOT_RUNNING.getDescription()));
		assertThat(capturedForUpdate.isRunning(), equalTo(CONVERTED_NOT_RUNNING.isRunning()));
		assertThat(capturedForUpdate.getCronExpression(), equalTo(CONVERTED_NOT_RUNNING.getCronExpression()));
	}

	@Test
	public void jobForgetAndStoppedWhenDeleted() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_RUNNING);
		mockRead(CONVERTED_RUNNING, EXISTING_RUNNING);
		mockJobFactory(EXISTING_RUNNING, mockJob("foo"));

		// when
		schedulerFacade.delete(DUMMY_SCHEDULED_TASK);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_RUNNING);
		inOrder.verify(store).delete(EXISTING_RUNNING);
		inOrder.verify(jobFactory).create(EXISTING_RUNNING);
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJob = jobCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
	}

	@Test
	public void jobForgetAndNotStoppedWhenDeleted() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_NOT_RUNNING);
		mockRead(CONVERTED_NOT_RUNNING, EXISTING_NOT_RUNNING);

		// when
		schedulerFacade.delete(DUMMY_SCHEDULED_TASK);

		// then
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_NOT_RUNNING);
		inOrder.verify(store).delete(EXISTING_NOT_RUNNING);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allScheduledTasksRead() throws Exception {
		// given
		when(store.list()) //
				.thenReturn(asList(EXISTING_RUNNING, EXISTING_NOT_RUNNING));
		mockConversion(EXISTING_RUNNING, DUMMY_SCHEDULED_TASK);
		mockConversion(EXISTING_NOT_RUNNING, DUMMY_SCHEDULED_TASK);

		// when
		final Iterable<ScheduledTask> readed = schedulerFacade.read();

		// then
		assertThat(size(readed), equalTo(2));

		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(store).list();
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(EXISTING_RUNNING);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(EXISTING_NOT_RUNNING);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskDetailsRead() throws Exception {
		// given
		mockConversion(DUMMY_SCHEDULED_TASK, CONVERTED_RUNNING);
		mockRead(CONVERTED_RUNNING, EXISTING_RUNNING);
		mockConversion(EXISTING_RUNNING, DUMMY_SCHEDULED_TASK);

		// when
		schedulerFacade.read(DUMMY_SCHEDULED_TASK);

		// then
		final InOrder inOrder = inOrder(scheduledTaskFacadeConverterFactory, store, jobFactory, schedulerService);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(DUMMY_SCHEDULED_TASK);
		inOrder.verify(store).read(CONVERTED_RUNNING);
		inOrder.verify(scheduledTaskFacadeConverterFactory).of(EXISTING_RUNNING);
		inOrder.verifyNoMoreInteractions();
	}

}
