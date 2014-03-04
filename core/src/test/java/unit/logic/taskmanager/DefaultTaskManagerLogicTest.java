package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Functions;

public class DefaultTaskManagerLogicTest {

	private static final long ID = 42L;
	private static final long ANOTHER_ID = 123L;
	private static final String DESCRIPTION = "the description";
	private static final String NEW_DESCRIPTION = "new description";
	private static final boolean ACTIVE_STATUS = true;
	private static final String CRON_EXPRESSION = "cron expression";
	private static final String NEW_CRON_EXPRESSION = "new cron expression";
	private static final String PROCESS_CLASS = "Dummy";
	private static final Iterable<String> VALUES = asList("foo", "bar");
	private static Map<String, String> PARAMETERS = uniqueIndex(VALUES, Functions.<String> identity());

	private ScheduledTaskFacade scheduledTaskFacade;
	private DefaultTaskManagerLogic taskManagerLogic;

	@Before
	public void setUp() throws Exception {
		scheduledTaskFacade = mock(ScheduledTaskFacade.class);
		taskManagerLogic = new DefaultTaskManagerLogic(scheduledTaskFacade);
	}

	@Test
	public void readEmailTaskCreated() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();

		// when
		taskManagerLogic.create(task);

		verify(scheduledTaskFacade).create(task);
		verifyNoMoreInteractions(scheduledTaskFacade);
	}

	@Test
	public void startWorkflowTaskCreated() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.withProcessClass(PROCESS_CLASS) //
				.withParameters(PARAMETERS) //
				.build();

		// when
		taskManagerLogic.create(task);

		verify(scheduledTaskFacade).create(task);
		verifyNoMoreInteractions(scheduledTaskFacade);
	}

	@Test
	public void readEmailTaskUpdated() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.withDescription(NEW_DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(NEW_CRON_EXPRESSION) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		verify(scheduledTaskFacade).update(task);
		verifyNoMoreInteractions(scheduledTaskFacade);
	}

	@Test
	public void startWorkflowTaskUpdated() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.withDescription(NEW_DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(NEW_CRON_EXPRESSION) //
				.withProcessClass(PROCESS_CLASS) //
				.withParameters(PARAMETERS) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		verify(scheduledTaskFacade).update(task);
		verifyNoMoreInteractions(scheduledTaskFacade);
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
	public void readEmailTaskDeleted() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		verify(scheduledTaskFacade).delete(task);
	}

	@Test
	public void startWorkflowTaskDeleted() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		verify(scheduledTaskFacade).delete(task);
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
		final ReadEmailTask first = ReadEmailTask.newInstance() //
				.withId(ID) //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		final ScheduledTask second = StartWorkflowTask.newInstance() //
				.withId(ANOTHER_ID) //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		when(scheduledTaskFacade.read()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<? extends Task> readed = taskManagerLogic.read();

		// then
		verify(scheduledTaskFacade, times(2)).read();
		verifyNoMoreInteractions(scheduledTaskFacade);

		assertThat(size(readed), equalTo(2));

		final Task firstReaded = get(readed, 0);
		assertThat(firstReaded, instanceOf(ReadEmailTask.class));
		assertThat(firstReaded.getId(), equalTo(ID));

		final Task secondReaded = get(readed, 1);
		assertThat(secondReaded, instanceOf(ScheduledTask.class));
		assertThat(secondReaded.getId(), equalTo(ANOTHER_ID));

	}

	@Test
	public void specificTaskTypeReaded() throws Exception {
		// given
		final ReadEmailTask first = ReadEmailTask.newInstance() //
				.withId(1L) //
				.build();
		final ScheduledTask second = StartWorkflowTask.newInstance() //
				.withId(2L) //
				.build();
		final ScheduledTask third = StartWorkflowTask.newInstance() //
				.withId(3L) //
				.build();
		when(scheduledTaskFacade.read()) //
				.thenReturn(asList(first, second, third));

		// when
		final Iterable<Task> readed = taskManagerLogic.read(StartWorkflowTask.class);

		// then
		verify(scheduledTaskFacade, times(2)).read();
		verifyNoMoreInteractions(scheduledTaskFacade);

		assertThat(size(readed), equalTo(2));

		final Task firstReaded = get(readed, 0);
		assertThat(firstReaded, instanceOf(StartWorkflowTask.class));
		assertThat(firstReaded.getId(), equalTo(2L));

		final Task secondReaded = get(readed, 1);
		assertThat(secondReaded, instanceOf(StartWorkflowTask.class));
		assertThat(secondReaded.getId(), equalTo(3L));
	}

	@Test
	public void readEmailTaskDetailsRead() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();
		final ReadEmailTask readed = ReadEmailTask.newInstance() //
				.withId(ID) //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				// add more details
				.build();
		when(scheduledTaskFacade.read(task)) //
				.thenReturn(readed);

		// when
		final ReadEmailTask detailed = taskManagerLogic.read(task, ReadEmailTask.class);

		// then
		verify(scheduledTaskFacade).read(task);
		verifyNoMoreInteractions(scheduledTaskFacade);

		assertThat(detailed, instanceOf(ReadEmailTask.class));
		assertThat(detailed.getId(), equalTo(ID));
		assertThat(detailed.getDescription(), equalTo(DESCRIPTION));
		assertThat(detailed.isActive(), equalTo(ACTIVE_STATUS));
		assertThat(detailed.getCronExpression(), equalTo(CRON_EXPRESSION));
	}

	@Test
	public void startWorkflowTaskDetailsRead() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.build();
		final StartWorkflowTask readed = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.withDescription(DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.withProcessClass(PROCESS_CLASS) //
				.build();
		when(scheduledTaskFacade.read(task)) //
				.thenReturn(readed);

		// when
		final StartWorkflowTask detailed = taskManagerLogic.read(task, StartWorkflowTask.class);

		// then
		verify(scheduledTaskFacade).read(task);
		verifyNoMoreInteractions(scheduledTaskFacade);

		assertThat(detailed, instanceOf(StartWorkflowTask.class));
		assertThat(detailed.getId(), equalTo(ID));
		assertThat(detailed.getDescription(), equalTo(DESCRIPTION));
		assertThat(detailed.isActive(), equalTo(ACTIVE_STATUS));
		assertThat(detailed.getCronExpression(), equalTo(CRON_EXPRESSION));
		assertThat(detailed.getProcessClass(), equalTo(PROCESS_CLASS));
	}

}
