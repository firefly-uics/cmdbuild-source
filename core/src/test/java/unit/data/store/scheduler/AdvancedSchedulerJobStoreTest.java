package unit.data.store.scheduler;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore.WORKFLOW_PARAM_CLASSNAME;
import static org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore.WORKFLOW_PARAM_ATTRIBUTES;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobParameter;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.base.Function;

@RunWith(MockitoJUnitRunner.class)
public class AdvancedSchedulerJobStoreTest {

	private static final Function<SchedulerJobParameter, String> BY_KEY = new Function<SchedulerJobParameter, String>() {

		@Override
		public String apply(final SchedulerJobParameter input) {
			return input.getKey();
		}

	};

	@Mock
	private Store<SchedulerJob> schedulerJobStore;

	@Mock
	private Store<SchedulerJobParameter> schedulerJobParameterStore;

	private AdvancedSchedulerJobStore store;

	@Before
	public void setUp() throws Exception {
		store = new AdvancedSchedulerJobStore(schedulerJobStore, schedulerJobParameterStore);
	}

	@Test
	public void workflowSchedulerJobCreated() throws Exception {
		// given
		final Map<String, String> parameters = newLinkedHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo\nlol");
		final WorkflowSchedulerJob newOne = new WorkflowSchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
				setCronExpression("the cron expression");
				setProcessClass("the process class");
				setParameters(parameters);
			}
		};
		when(schedulerJobStore.read(any(WorkflowSchedulerJob.class))) //
				.thenReturn(newOne);

		// when
		store.create(newOne);

		// then
		final ArgumentCaptor<SchedulerJobParameter> parameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).create(newOne);
		inOrder.verify(schedulerJobParameterStore, times(2)).create(parameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Map<String, SchedulerJobParameter> parametersByKey = uniqueIndex(parameterCaptor.getAllValues(), BY_KEY);

		final SchedulerJobParameter processClass = parametersByKey.get(WORKFLOW_PARAM_CLASSNAME);
		assertThat(processClass, not(nullValue(SchedulerJobParameter.class)));
		assertThat(processClass.getOwner(), equalTo(newOne.getId()));
		assertThat(processClass.getValue(), equalTo("the process class"));

		final SchedulerJobParameter processParameters = parametersByKey.get(WORKFLOW_PARAM_ATTRIBUTES);
		assertThat(processParameters, not(nullValue(SchedulerJobParameter.class)));
		assertThat(processClass.getOwner(), equalTo(newOne.getId()));
		assertThat(processParameters.getValue(), equalTo("foo=bar\nbar=baz\nbaz=foo\nlol"));
	}

	@Test
	public void workflowSchedulerJobReaded() throws Exception {
		// given
		final SchedulerJob existingOne = new WorkflowSchedulerJob(42L);
		when(schedulerJobStore.read(existingOne)) //
				.thenReturn(existingOne);
		when(schedulerJobStore.list()) //
				.thenReturn(asList(existingOne));
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(WORKFLOW_PARAM_CLASSNAME) //
								.withValue("the process class") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(WORKFLOW_PARAM_ATTRIBUTES) //
								.withValue("foo=bar\nbar=baz") //
								.build() //
						));

		// when
		final SchedulerJob readed = store.read(existingOne);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).read(existingOne);
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable captured = groupableCaptor.getValue();
		assertThat(captured.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(captured.getGroupAttributeValue(), equalTo((Object) existingOne.getIdentifier()));
		
		assertThat(readed, instanceOf(WorkflowSchedulerJob.class));
		final WorkflowSchedulerJob workflowSchedulerJob = WorkflowSchedulerJob.class.cast(readed);
		assertThat(workflowSchedulerJob.getId(), equalTo(42L));
		assertThat(workflowSchedulerJob.getProcessClass(), equalTo("the process class"));

		final Map<String, String> expected = newHashMap();
		expected.put("foo", "bar");
		expected.put("bar", "baz");
		assertThat(workflowSchedulerJob.getParameters(), equalTo(expected));
	}

	@Test
	public void workflowSchedulerJobUpdated() throws Exception {
		// given
		final Map<String, String> updatedParameters = newLinkedHashMap();
		updatedParameters.put("foo", "bar");
		updatedParameters.put("bar", "baz");
		updatedParameters.put("baz", "foo");
		final WorkflowSchedulerJob updatedOne = new WorkflowSchedulerJob(42L) {
			{
				setDescription("the new description");
				setRunning(true);
				setCronExpression("the new cron expression");
				setProcessClass("the new process class");
				setParameters(updatedParameters);
			}
		};
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList(SchedulerJobParameter.newInstance() //
						.withOwner(123L) //
						.withKey(WORKFLOW_PARAM_CLASSNAME) //
						.withValue("the old process class") //
						.build()));

		// when
		store.update(updatedOne);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final ArgumentCaptor<SchedulerJobParameter> createdParameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final ArgumentCaptor<SchedulerJobParameter> updatedParameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).update(updatedOne);
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verify(schedulerJobParameterStore).create(createdParameterCaptor.capture());
		inOrder.verify(schedulerJobParameterStore).update(updatedParameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) updatedOne.getIdentifier()));

		final SchedulerJobParameter processClass = updatedParameterCaptor.getValue();
		assertThat(processClass, not(nullValue(SchedulerJobParameter.class)));
		assertThat(processClass.getOwner(), equalTo(updatedOne.getId()));
		assertThat(processClass.getValue(), equalTo("the new process class"));

		final SchedulerJobParameter processParameters = createdParameterCaptor.getValue();
		assertThat(processParameters, not(nullValue(SchedulerJobParameter.class)));
		assertThat(processClass.getOwner(), equalTo(updatedOne.getId()));
		assertThat(processParameters.getValue(), equalTo("foo=bar\nbar=baz\nbaz=foo"));
	}

	@Test
	public void workflowSchedulerJobDeleted() throws Exception {
		// given
		final WorkflowSchedulerJob existingOne = new WorkflowSchedulerJob(42L);
		when(schedulerJobStore.read(existingOne)) //
				.thenReturn(existingOne);
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList(SchedulerJobParameter.newInstance() //
						.withId(123L) //
						.withOwner(42L) //
						.withKey("foo") //
						.withValue("bar") //
						.build()));

		// when
		store.delete(existingOne);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final ArgumentCaptor<SchedulerJobParameter> parameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verify(schedulerJobParameterStore).delete(parameterCaptor.capture());
		inOrder.verify(schedulerJobStore).delete(existingOne);
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) existingOne.getIdentifier()));

		final SchedulerJobParameter capturedParameter = parameterCaptor.getValue();
		assertThat(capturedParameter.getId(), equalTo(123L));
		assertThat(capturedParameter.getOwner(), equalTo(existingOne.getId()));
		assertThat(capturedParameter.getKey(), equalTo("foo"));
		assertThat(capturedParameter.getValue(), equalTo("bar"));
	}

	@Test
	public void workflowSchedulerJobListed() throws Exception {
		// given
		final SchedulerJob existingOne = new WorkflowSchedulerJob(42L);
		when(schedulerJobStore.list()) //
				.thenReturn(asList(existingOne));
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(WORKFLOW_PARAM_CLASSNAME) //
								.withValue("the process class") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(WORKFLOW_PARAM_ATTRIBUTES) //
								.withValue("foo=bar\nbar=baz") //
								.build() //
						));

		// when
		final List<SchedulerJob> elements = store.list();

		assertThat(elements, hasSize(1));
		final SchedulerJob element = elements.get(0);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).list();
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) existingOne.getIdentifier()));

		assertThat(element, instanceOf(WorkflowSchedulerJob.class));
		final WorkflowSchedulerJob workflowSchedulerJob = WorkflowSchedulerJob.class.cast(element);
		assertThat(workflowSchedulerJob.getId(), equalTo(42L));
		assertThat(workflowSchedulerJob.getProcessClass(), equalTo("the process class"));

		final Map<String, String> expected = newHashMap();
		expected.put("foo", "bar");
		expected.put("bar", "baz");
		assertThat(workflowSchedulerJob.getParameters(), equalTo(expected));
	}

	@Test
	public void schedulerJobListedByGroup() throws Exception {
		// given
		final SchedulerJob existingOne = new WorkflowSchedulerJob(42L);
		when(schedulerJobStore.list(any(Groupable.class))) //
				.thenReturn(asList(existingOne));
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList(SchedulerJobParameter.newInstance() //
						.withId(123L) //
						.withOwner(42L) //
						.withKey("foo") //
						.withValue("bar") //
						.build()));
		final Groupable groupable = mock(Groupable.class);

		// when
		store.list(groupable);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).list(any(Groupable.class));
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) existingOne.getIdentifier()));
	}

}
