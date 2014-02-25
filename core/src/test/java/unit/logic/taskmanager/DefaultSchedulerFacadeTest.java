package unit.logic.taskmanager;

import static com.google.common.base.Functions.identity;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.scheduler.JobFactory;
import org.cmdbuild.logic.taskmanager.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.model.scheduler.SchedulerJob;
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

public class DefaultSchedulerFacadeTest {

	private static final Function<String, String> IDENTITY = identity();
	private static final Map<String, String> PARAMETERS = Maps.uniqueIndex(asList("foo", "bar", "baz"), IDENTITY);

	private final SchedulerJob NEW_ONE = new SchedulerJob() {

		{
			setDescription("the description");
			setRunning(true);
			setDetail("Dummy");
			setCronExpression("the cron expression");
			setLegacyParameters(PARAMETERS);
		}

	};

	private final SchedulerJob EXISTING = new SchedulerJob(42L) {

		{
			setDescription("the description");
			setRunning(true);
			setCronExpression("the cron expression");
			setDetail("Dummy");
			setLegacyParameters(PARAMETERS);
		}

	};

	private final SchedulerJob UPDATED = EXISTING;

	private final Storable STORED = new Storable() {

		@Override
		public String getIdentifier() {
			return "42";
		}

	};

	private Store<SchedulerJob> store;
	private JobFactory jobFactory;
	private SchedulerService schedulerService;
	private SchedulerFacade schedulerFacade;

	@Before
	@SuppressWarnings("unchecked")
	public void setUp() throws Exception {
		store = mock(Store.class);
		jobFactory = mock(JobFactory.class);
		schedulerService = mock(SchedulerService.class);
		schedulerFacade = new DefaultSchedulerFacade(store, schedulerService, jobFactory);
	}

	@After
	public void verifyNoMoreInteractionsOnMocks() throws Exception {
		verifyNoMoreInteractions(store, jobFactory, schedulerService);
	}

	@Test
	public void jobPersistedAndStartedWhenCreated() throws Exception {
		// given
		when(store.create(NEW_ONE)) //
				.thenReturn(STORED);
		when(store.read(STORED)) //
				.thenReturn(EXISTING);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(EXISTING)) //
				.thenReturn(job);

		// when
		schedulerFacade.create(NEW_ONE);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).create(eq(NEW_ONE));
		inOrder.verify(store).read(eq(STORED));
		inOrder.verify(jobFactory).create(eq(EXISTING));
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJob = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(NEW_ONE.getCronExpression())));
	}

	@Test
	public void jobPersistedAndNotStartedWhenCreated() throws Exception {
		// given
		NEW_ONE.setRunning(false);
		EXISTING.setRunning(false);
		when(store.create(NEW_ONE)) //
				.thenReturn(STORED);
		when(store.read(STORED)) //
				.thenReturn(EXISTING);

		// when
		schedulerFacade.create(NEW_ONE);

		// then
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).create(eq(NEW_ONE));
		inOrder.verify(store).read(eq(STORED));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void jobStoppedPersistedAndNotStartedWhenUpdated() throws Exception {
		// given
		final SchedulerJob READED = new SchedulerJob(UPDATED.getId());
		READED.setRunning(true);
		UPDATED.setRunning(false);
		final Job job = mock(Job.class);
		when(store.read(UPDATED)) //
				.thenReturn(READED);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(UPDATED)) //
				.thenReturn(job);

		// when
		schedulerFacade.update(UPDATED);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(UPDATED));
		inOrder.verify(jobFactory).create(eq(UPDATED));
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForUpdate, equalTo(READED));
		assertThat(capturedForUpdate.getDescription(), equalTo(UPDATED.getDescription()));
		assertThat(capturedForUpdate.isRunning(), equalTo(UPDATED.isRunning()));
		assertThat(capturedForUpdate.getCronExpression(), equalTo(UPDATED.getCronExpression()));

		final Job capturedJobForRemove = jobCaptor.getAllValues().get(0);
		assertThat(capturedJobForRemove.getName(), equalTo("foo"));
	}

	@Test
	public void jobStoppedPersistedAndStartedWhenUpdated() throws Exception {
		// given
		final SchedulerJob READED = new SchedulerJob(UPDATED.getId());
		READED.setRunning(true);
		when(store.read(UPDATED)) //
				.thenReturn(READED);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(any(SchedulerJob.class))) //
				.thenReturn(job);

		// when
		schedulerFacade.update(UPDATED);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(UPDATED));
		inOrder.verify(jobFactory).create(eq(UPDATED));
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForUpdate, equalTo(READED));
		assertThat(capturedForUpdate.getDescription(), equalTo(UPDATED.getDescription()));
		assertThat(capturedForUpdate.isRunning(), equalTo(UPDATED.isRunning()));
		assertThat(capturedForUpdate.getCronExpression(), equalTo(UPDATED.getCronExpression()));

		final Job capturedJobForRemove = jobCaptor.getAllValues().get(0);
		assertThat(capturedJobForRemove.getName(), equalTo("foo"));

		final Job capturedJobForAdd = jobCaptor.getAllValues().get(1);
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJobForAdd.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(UPDATED.getCronExpression())));
	}

	@Test
	public void jobNotStoppedPersistedAndNotStartedWhenUpdated() throws Exception {
		// given
		EXISTING.setRunning(false);
		final SchedulerJob UPDATED = EXISTING;
		UPDATED.setDescription("the new description");
		UPDATED.setCronExpression("the new cron expression");
		when(store.read(EXISTING)) //
				.thenReturn(EXISTING);

		// when
		schedulerFacade.update(UPDATED);

		// then
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(EXISTING));
		inOrder.verify(store).update(eq(UPDATED));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void jobNotStoppedPersistedAndStartedWhenUpdated() throws Exception {
		// given
		final SchedulerJob READED = new SchedulerJob(UPDATED.getId());
		READED.setRunning(false);
		final Job job = mock(Job.class);
		when(store.read(EXISTING)) //
				.thenReturn(READED);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(any(SchedulerJob.class))) //
				.thenReturn(job);

		// when
		schedulerFacade.update(UPDATED);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(UPDATED));
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForUpdate, equalTo(READED));
		assertThat(capturedForUpdate.getDescription(), equalTo(UPDATED.getDescription()));
		assertThat(capturedForUpdate.isRunning(), equalTo(UPDATED.isRunning()));
		assertThat(capturedForUpdate.getCronExpression(), equalTo(UPDATED.getCronExpression()));

		final Job capturedJobForAdd = jobCaptor.getAllValues().get(0);
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJobForAdd.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at(UPDATED.getCronExpression())));
	}

	@Test
	public void jobForgetAndStoppedWhenDeleted() throws Exception {
		// given
		when(store.read(EXISTING)) //
				.thenReturn(EXISTING);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("foo");
		when(jobFactory.create(EXISTING)) //
				.thenReturn(job);

		// when
		schedulerFacade.delete(EXISTING);

		// then
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(EXISTING));
		inOrder.verify(store).delete(eq(EXISTING));
		inOrder.verify(jobFactory).create(eq(EXISTING));
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJob = jobCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
	}

	@Test
	public void jobForgetAndNotStoppedWhenDeleted() throws Exception {
		// given
		EXISTING.setRunning(false);
		when(store.read(EXISTING)) //
				.thenReturn(EXISTING);

		// when
		schedulerFacade.delete(EXISTING);

		// then
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(eq(EXISTING));
		inOrder.verify(store).delete(eq(EXISTING));
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void allScheduledJobsRead() throws Exception {
		// given
		final SchedulerJob schedulerJob = new SchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
			}
		};
		when(store.list()) //
				.thenReturn(asList(schedulerJob));

		// when
		final Iterable<SchedulerJob> schedulerJobs = schedulerFacade.read();

		// then
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).list();
		inOrder.verifyNoMoreInteractions();

		assertThat(size(schedulerJobs), equalTo(1));

		final SchedulerJob onlySchedulerJob = get(schedulerJobs, 0);
		assertThat(onlySchedulerJob.getId(), equalTo(42L));
		assertThat(onlySchedulerJob.getDescription(), equalTo("the description"));
		assertThat(onlySchedulerJob.isRunning(), equalTo(true));
	}

	@Test
	public void workflowSchedulerJobDetailsRead() throws Exception {
		// given
		final SchedulerJob READED = new SchedulerJob(42L);
		when(store.read(EXISTING)) //
				.thenReturn(READED);

		// when
		final SchedulerJob readed = schedulerFacade.read(EXISTING);

		// then
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(EXISTING);
		inOrder.verifyNoMoreInteractions();

		assertThat(readed, equalTo(READED));
	}

}
