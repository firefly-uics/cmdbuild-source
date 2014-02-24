package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
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
import org.cmdbuild.logic.taskmanager.DefaultSchedulerFacade;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
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

public class DefaultSchedulerFacadeTest {

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

	@Test
	public void schedulerJobCreated() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();
		values.put("foo", "bar");
		values.put("bar", "baz");
		values.put("baz", "foo");
		final SchedulerJob schedulerJob = new SchedulerJob(42L) {
			{
				setDescription("the description");
				setDetail("Dummy");
				setCronExpression("cron expression");
				setLegacyParameters(values);
			}
		};
		final Storable created = mock(Storable.class);
		when(created.getIdentifier()) //
				.thenReturn("42");
		when(store.create(any(SchedulerJob.class))) //
				.thenReturn(created);
		final SchedulerJob readed = new SchedulerJob(42L) {
			{
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
		schedulerFacade.create(schedulerJob);

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
		assertThat(capturedForStore.getDescription(), equalTo("the description"));
		assertThat(capturedForStore.getDetail(), equalTo("Dummy"));
		assertThat(capturedForStore.getLegacyParameters(), equalTo(values));

		final SchedulerJob capturedForJobFactory = schedulerJobCaptor.getAllValues().get(1);
		assertThat(capturedForJobFactory.getDescription(), equalTo("the description"));
		assertThat(capturedForJobFactory.getDetail(), equalTo("Dummy"));
		assertThat(capturedForJobFactory.getLegacyParameters(), equalTo(values));

		final Job capturedJob = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("foo"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at("cron expression")));
	}

	@Test
	public void schedulerJobUpdates() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();
		values.put("foo", "bar");
		values.put("bar", "baz");
		values.put("baz", "foo");
		final SchedulerJob schedulerJob = new SchedulerJob(42L) {
			{
				setDescription("the new description");
				setCronExpression("the new cron expression");
				setLegacyParameters(values);
			}
		};
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("42");
		when(jobFactory.create(schedulerJob)) //
				.thenReturn(job);
		when(store.read(schedulerJob)) //
				.thenReturn(schedulerJob);

		// when
		schedulerFacade.update(schedulerJob);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final ArgumentCaptor<Trigger> triggerCaptor = ArgumentCaptor.forClass(Trigger.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verify(store).read(schedulerJobCaptor.capture());
		inOrder.verify(store).update(schedulerJobCaptor.capture());
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).add(jobCaptor.capture(), triggerCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Job capturedJobForRemove = jobCaptor.getAllValues().get(0);
		assertThat(capturedJobForRemove.getName(), equalTo("42"));

		final SchedulerJob capturedForRead = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForRead.getId(), equalTo(42L));

		final SchedulerJob capturedForUpdate = schedulerJobCaptor.getAllValues().get(1);
		assertThat(capturedForUpdate.getId(), equalTo(42L));
		assertThat(capturedForUpdate.getDescription(), equalTo("the new description"));
		assertThat(capturedForUpdate.getCronExpression(), equalTo("the new cron expression"));
		assertThat(capturedForUpdate.getLegacyParameters(), equalTo(values));

		final Job capturedJobForAdd = jobCaptor.getValue();
		final Trigger capturedTrigger = triggerCaptor.getValue();
		assertThat(capturedJobForAdd.getName(), equalTo("42"));
		assertThat(capturedTrigger, equalTo((Trigger) RecurringTrigger.at("the new cron expression")));
	}

	@Test
	public void schedulerJobDeleted() throws Exception {
		// given
		final SchedulerJob schedulerJob = new SchedulerJob(42L);
		when(store.read(schedulerJob)) //
				.thenReturn(schedulerJob);
		final Job job = mock(Job.class);
		when(job.getName()) //
				.thenReturn("42");
		when(jobFactory.create(schedulerJob)) //
				.thenReturn(job);

		// when
		schedulerFacade.delete(schedulerJob);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		final ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
		final InOrder inOrder = inOrder(store, jobFactory, schedulerService);
		inOrder.verify(store).read(schedulerJobCaptor.capture());
		inOrder.verify(store).delete(schedulerJobCaptor.capture());
		inOrder.verify(jobFactory).create(schedulerJobCaptor.capture());
		inOrder.verify(schedulerService).remove(jobCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final SchedulerJob capturedForRead = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForRead.getId(), equalTo(42L));

		final SchedulerJob capturedForDelete = schedulerJobCaptor.getAllValues().get(1);
		assertThat(capturedForDelete.getId(), equalTo(42L));

		final SchedulerJob capturedForJobFactory = schedulerJobCaptor.getAllValues().get(2);
		assertThat(capturedForJobFactory.getId(), equalTo(42L));

		final Job capturedJob = jobCaptor.getValue();
		assertThat(capturedJob.getName(), equalTo("42"));
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

}
