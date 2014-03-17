package unit.logic.taskmanager;

import static com.google.common.collect.Iterables.size;
import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.cmdbuild.logic.taskmanager.DefaultTaskManagerLogic;
import org.cmdbuild.logic.taskmanager.LogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.SchedulerFacade;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

public class DefaultTaskManagerLogicTest {

	private static final class DummyStorableTask extends org.cmdbuild.data.store.task.Task {

		public static Builder<DummyStorableTask> newInstance() {
			return new Builder<DummyStorableTask>() {

				@Override
				public DummyStorableTask doBuild() {
					return new DummyStorableTask(this);
				}

			};
		}

		private DummyStorableTask(final Builder<? extends org.cmdbuild.data.store.task.Task> builder) {
			super(builder);
		}

		@Override
		public void accept(final TaskVisitor visitor) {
			// nothing to do
		}

	}

	private static final org.cmdbuild.data.store.task.Task DUMMY_STORABLE_TASK = DummyStorableTask.newInstance() //
			.build();

	private static final Task DUMMY_TASK = mock(Task.class);

	private static final long ID = 42L;
	private static final long ANOTHER_ID = 123L;
	private static final String DESCRIPTION = "the description";
	private static final String NEW_DESCRIPTION = "new description";
	private static final boolean ACTIVE_STATUS = true;
	private static final String CRON_EXPRESSION = "cron expression";
	private static final String NEW_CRON_EXPRESSION = "new cron expression";

	private LogicAndStoreConverter converter;
	private LogicAndStoreConverter.LogicAsSourceConverter logicAsSourceConverter;
	private LogicAndStoreConverter.StoreAsSourceConverter storeAsSourceConverter;
	private Store<org.cmdbuild.data.store.task.Task> store;
	private SchedulerFacade scheduledTaskFacade;
	private DefaultTaskManagerLogic taskManagerLogic;

	@Before
	public void setUp() throws Exception {
		logicAsSourceConverter = mock(LogicAndStoreConverter.LogicAsSourceConverter.class);
		when(logicAsSourceConverter.toStore()) //
				.thenReturn(DUMMY_STORABLE_TASK);

		storeAsSourceConverter = mock(LogicAndStoreConverter.StoreAsSourceConverter.class);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(DUMMY_TASK);

		converter = mock(LogicAndStoreConverter.class);
		when(converter.from(any(Task.class))) //
				.thenReturn(logicAsSourceConverter);
		when(converter.from(any(org.cmdbuild.data.store.task.Task.class))) //
				.thenReturn(storeAsSourceConverter);

		store = mock(Store.class);

		scheduledTaskFacade = mock(SchedulerFacade.class);

		taskManagerLogic = new DefaultTaskManagerLogic(converter, store, scheduledTaskFacade);
	}

	@Test
	public void scheduledTaskCreated() throws Exception {
		// given
		final ReadEmailTask newOne = ReadEmailTask.newInstance().build();
		final org.cmdbuild.data.store.task.ReadEmailTask createdOne = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance().build();
		final ReadEmailTask convertedAfterRead = ReadEmailTask.newInstance().build();
		final Storable storable = mock(Storable.class);
		when(store.create(DUMMY_STORABLE_TASK)) //
				.thenReturn(storable);
		when(store.read(storable)) //
				.thenReturn(createdOne);
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(convertedAfterRead);

		// when
		taskManagerLogic.create(newOne);

		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(converter).from(newOne);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).create(DUMMY_STORABLE_TASK);
		inOrder.verify(store).read(storable);
		inOrder.verify(converter).from(createdOne);
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).create(convertedAfterRead);
		inOrder.verifyNoMoreInteractions();
	}

	@Test
	public void scheduledTaskUpdated() throws Exception {
		// given
		when(storeAsSourceConverter.toLogic()) //
				.thenReturn(ReadEmailTask.newInstance() //
						.withId(ID) //
						.withDescription("should be deleted from scheduler facade") //
						.build() //
				);
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.withDescription(NEW_DESCRIPTION) //
				.withActiveStatus(ACTIVE_STATUS) //
				.withCronExpression(NEW_CRON_EXPRESSION) //
				.build();

		// when
		taskManagerLogic.update(task);

		// then
		final ArgumentCaptor<ScheduledTask> scheduledTaskCaptor = ArgumentCaptor.forClass(ScheduledTask.class);
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).read(DUMMY_STORABLE_TASK);
		inOrder.verify(converter).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verify(scheduledTaskFacade).delete(scheduledTaskCaptor.capture());
		inOrder.verify(store).update(DUMMY_STORABLE_TASK);
		inOrder.verify(scheduledTaskFacade).create(task);
		inOrder.verifyNoMoreInteractions();

		final ScheduledTask captured = scheduledTaskCaptor.getValue();
		assertThat(captured.getDescription(), equalTo("should be deleted from scheduler facade"));
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
	public void scheduledTaskDeleted() throws Exception {
		// given
		final ScheduledTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();

		// when
		taskManagerLogic.delete(task);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(scheduledTaskFacade).delete(task);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).delete(DUMMY_STORABLE_TASK);
		inOrder.verifyNoMoreInteractions();
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
		final org.cmdbuild.data.store.task.ReadEmailTask first = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance().withId(ID) //
				.withDescription(DESCRIPTION) //
				.withRunningStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		final org.cmdbuild.data.store.task.StartWorkflowTask second = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance().withId(ANOTHER_ID) //
				.withDescription(DESCRIPTION) //
				.withRunningStatus(ACTIVE_STATUS) //
				.withCronExpression(CRON_EXPRESSION) //
				.build();
		when(store.list()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<? extends Task> readed = taskManagerLogic.read();

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(store).list();
		inOrder.verify(converter, times(2)).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();

		assertThat(size(readed), equalTo(2));
	}

	@Test
	public void specificTaskTypeReaded() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask first = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(1L) //
				.build();
		final org.cmdbuild.data.store.task.StartWorkflowTask second = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(2L) //
				.build();
		when(store.list()) //
				.thenReturn(asList(first, second));

		// when
		final Iterable<Task> readed = taskManagerLogic.read(Task.class);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(store).list();
		inOrder.verify(converter, times(2)).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();

		assertThat(size(readed), equalTo(2));
	}

	@Test
	public void taskDetailsRead() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(ID) //
				.build();
		final org.cmdbuild.data.store.task.ReadEmailTask stored = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(ID) //
				.build();
		when(store.read(any(Storable.class))) //
				.thenReturn(stored);

		// when
		taskManagerLogic.read(task, Task.class);

		// then
		final InOrder inOrder = inOrder(converter, logicAsSourceConverter, storeAsSourceConverter, store,
				scheduledTaskFacade);
		inOrder.verify(converter).from(task);
		inOrder.verify(logicAsSourceConverter).toStore();
		inOrder.verify(store).read(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(converter).from(any(org.cmdbuild.data.store.task.Task.class));
		inOrder.verify(storeAsSourceConverter).toLogic();
		inOrder.verifyNoMoreInteractions();
	}

	@Test(expected = IllegalArgumentException.class)
	public void cannotReadTaskDetailsWithoutAnId() throws Exception {
		// given
		final Task task = mock(Task.class);
		when(task.getId()) //
				.thenReturn(null);

		// when
		taskManagerLogic.read(task, Task.class);
	}

}
