package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.model.scheduler.SchedulerJob;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.google.common.collect.Maps;

public class DefaultTaskManagerLogicTest {

	private SchedulerFacade schedulerFacade;
	private DefaultTaskManagerLogic taskManagerLogic;

	@Before
	public void setUp() throws Exception {
		schedulerFacade = mock(SchedulerFacade.class);
		taskManagerLogic = new DefaultTaskManagerLogic(schedulerFacade);
	}

	@Test
	public void startWorkflowTaskCreated() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();
		values.put("foo", "bar");
		values.put("bar", "baz");
		values.put("baz", "foo");
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withDescription("the description") //
				.withActiveStatus(true) //
				.withProcessClass("Dummy") //
				.withCronExpression("cron expression") //
				.withParameters(values) //
				.build();

		// when
		taskManagerLogic.create(task);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		verify(schedulerFacade).create(schedulerJobCaptor.capture());
		verifyNoMoreInteractions(schedulerFacade);

		final SchedulerJob capturedForStore = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForStore.getDescription(), equalTo("the description"));
		assertThat(capturedForStore.getDetail(), equalTo("Dummy"));
		assertThat(capturedForStore.getLegacyParameters(), equalTo(values));
	}

	@Test
	public void startWorkflowTaskUpdated() throws Exception {
		// given
		final Map<String, String> values = Maps.newHashMap();
		values.put("foo", "bar");
		values.put("bar", "baz");
		values.put("baz", "foo");
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("new description") //
				.withCronExpression("new cron expression") //
				.withParameters(values) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		verify(schedulerFacade).update(schedulerJobCaptor.capture());
		verifyNoMoreInteractions(schedulerFacade);

		final SchedulerJob capturedForStore = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForStore.getId(), equalTo(42L));
		assertThat(capturedForStore.getDescription(), equalTo("new description"));
		assertThat(capturedForStore.getCronExpression(), equalTo("new cron expression"));
		assertThat(capturedForStore.getLegacyParameters(), equalTo(values));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotUpdateTaskWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.update(task);
	}

	@Test
	public void startWorkflowTaskDeleted() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		verify(schedulerFacade).delete(schedulerJobCaptor.capture());
		verifyNoMoreInteractions(schedulerFacade);

		final SchedulerJob capturedForStore = schedulerJobCaptor.getAllValues().get(0);
		assertThat(capturedForStore.getId(), equalTo(42L));
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotDeleteTaskWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.delete(task);
	}

	@Test
	public void allTasksRead() throws Exception {
		// given
		final SchedulerJob schedulerJob = new SchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
			}
		};
		when(schedulerFacade.read()) //
				.thenReturn(asList(schedulerJob));

		// when
		final Iterable<? extends Task> tasks = taskManagerLogic.read();

		// then
		verify(schedulerFacade).read();
		verifyNoMoreInteractions(schedulerFacade);

		assertThat(size(tasks), equalTo(1));

		final Task onlyTask = get(tasks, 0);
		assertThat(onlyTask.getId(), equalTo(42L));
		assertThat(onlyTask.getDescription(), equalTo("the description"));
		assertThat(onlyTask.isActive(), equalTo(true));
	}

	@Test
	public void startWorkflowTaskDetailsRead() throws Exception {
		// given
		final StartWorkflowTask existing = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.build();
		final SchedulerJob readed = new SchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
				setCronExpression("the cron expression");
				setDetail("the process class");
			}
		};
		when(schedulerFacade.read(any(SchedulerJob.class))) //
				.thenReturn(readed);

		// when
		final StartWorkflowTask detailed = taskManagerLogic.read(existing, StartWorkflowTask.class);

		// then
		final ArgumentCaptor<SchedulerJob> schedulerJobCaptor = ArgumentCaptor.forClass(SchedulerJob.class);
		verify(schedulerFacade).read(schedulerJobCaptor.capture());
		verifyNoMoreInteractions(schedulerFacade);

		final SchedulerJob captured = schedulerJobCaptor.getValue();
		assertThat(captured.getId(), equalTo(42L));

		assertThat(detailed.getId(), equalTo(42L));
		assertThat(detailed.getDescription(), equalTo("the description"));
		assertThat(detailed.isActive(), equalTo(true));
		assertThat(detailed.getProcessClass(), equalTo("the process class"));
		assertThat(detailed.getCronExpression(), equalTo("the cron expression"));
	}

}
