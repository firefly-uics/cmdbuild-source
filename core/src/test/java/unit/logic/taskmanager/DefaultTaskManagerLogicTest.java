package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.ScheduledTaskFacade;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Ignore;
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
		final ScheduledTask first = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.withDescription(DESCRIPTION).withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.withProcessClass(PROCESS_CLASS) //
				.build();
		final ScheduledTask second = StartWorkflowTask.newInstance() //
				.withId(ANOTHER_ID) //
				.withDescription(DESCRIPTION).withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.withProcessClass(PROCESS_CLASS) //
				.build();
		when(scheduledTaskFacade.read()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<? extends Task> readed = taskManagerLogic.read();

		// then
		verify(scheduledTaskFacade).read();
		verifyNoMoreInteractions(scheduledTaskFacade);

		assertThat(size(readed), equalTo(2));

		final Task firstReaded = get(readed, 0);
		assertThat(firstReaded.getId(), equalTo(ID));

		final Task secondReaded = get(readed, 1);
		assertThat(secondReaded.getId(), equalTo(ANOTHER_ID));

	}

	@Ignore("TODO - more task types needs to be implemented first")
	@Test
	public void specificTaskTypeReaded() throws Exception {
		fail();
	}

	@Test
	public void startWorkflowTaskDetailsRead() throws Exception {
		// given
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.build();
		final StartWorkflowTask readed = StartWorkflowTask.newInstance() //
				.withId(ID) //
				.withDescription(DESCRIPTION).withActiveStatus(ACTIVE_STATUS) //
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
