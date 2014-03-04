package unit.data.store.scheduler;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.util.Arrays.asList;
import static org.cmdbuild.data.store.scheduler.SchedulerJobParameterConstants.SCHEDULER_ID;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore;
import org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore.ReadEmail;
import org.cmdbuild.data.store.scheduler.AdvancedSchedulerJobStore.StartWorkflow;
import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
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

		final SchedulerJobParameter processClass = parametersByKey.get(StartWorkflow.CLASSNAME);
		assertThat(processClass, not(nullValue(SchedulerJobParameter.class)));
		assertThat(processClass.getOwner(), equalTo(newOne.getId()));
		assertThat(processClass.getValue(), equalTo("the process class"));

		final SchedulerJobParameter processParameters = parametersByKey.get(StartWorkflow.ATTRIBUTES);
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
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(StartWorkflow.CLASSNAME) //
								.withValue("the process class") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(StartWorkflow.ATTRIBUTES) //
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
						.withKey(StartWorkflow.CLASSNAME) //
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
								.withKey(StartWorkflow.CLASSNAME) //
								.withValue("the process class") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(StartWorkflow.ATTRIBUTES) //
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

	@Test
	public void emailServiceSchedulerCreated() throws Exception {
		// given
		final EmailServiceSchedulerJob newOne = new EmailServiceSchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
				setCronExpression("the cron expression");
				setEmailAccount("email account");
				setNotificationActive(true);
				setRegexFromFilter("regex from filter");
				setRegexSubjectFilter("regex subject filter");
				setAttachmentsActive(true);
				setWorkflowActive(true);
				setWorkflowClassName("the workflow class name");
				setWorkflowFieldsMapping("the workflow fields mapping");
				setWorkflowAdvanceable(true);
				setAttachmentsStorableToWorkflow(true);
			}
		};
		when(schedulerJobStore.read(any(EmailServiceSchedulerJob.class))) //
				.thenReturn(newOne);

		// when
		store.create(newOne);

		// then
		final ArgumentCaptor<SchedulerJobParameter> parameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).create(newOne);
		inOrder.verify(schedulerJobParameterStore, times(10)).create(parameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Map<String, SchedulerJobParameter> parametersByKey = uniqueIndex(parameterCaptor.getAllValues(), BY_KEY);

		assertParameter(parametersByKey, newOne, ReadEmail.ACCOUNT_NAME, "email account");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_NOTIFICATION_ACTIVE, "true");
		assertParameter(parametersByKey, newOne, ReadEmail.FILTER_FROM_REGEX, "regex from filter");
		assertParameter(parametersByKey, newOne, ReadEmail.FILTER_SUBJECT_REGEX, "regex subject filter");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_ATTACHMENTS_ACTIVE, "true");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_WORKFLOW_ACTIVE, "true");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_WORKFLOW_CLASS_NAME, "the workflow class name");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING, "the workflow fields mapping");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_WORKFLOW_ADVANCE, "true");
		assertParameter(parametersByKey, newOne, ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE, "true");
	}

	@Test
	public void emailServiceSchedulerJobReaded() throws Exception {
		// given
		final SchedulerJob existingOne = new EmailServiceSchedulerJob(42L);
		when(schedulerJobStore.read(existingOne)) //
				.thenReturn(existingOne);
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.ACCOUNT_NAME) //
								.withValue("the account name") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_NOTIFICATION_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.FILTER_FROM_REGEX) //
								.withValue("regex from filter") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.FILTER_SUBJECT_REGEX) //
								.withValue("regex subject filter") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_ATTACHMENTS_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_CLASS_NAME) //
								.withValue("workflow class name") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING) //
								.withValue("workflow fields mapping") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ADVANCE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE) //
								.withValue("true") //
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

		assertThat(readed, instanceOf(EmailServiceSchedulerJob.class));
		final EmailServiceSchedulerJob emailServiceSchedulerJob = EmailServiceSchedulerJob.class.cast(readed);
		assertThat(emailServiceSchedulerJob.getId(), equalTo(42L));
		assertThat(emailServiceSchedulerJob.getEmailAccount(), equalTo("the account name"));
		assertThat(emailServiceSchedulerJob.isNotificationActive(), equalTo(true));
		assertThat(emailServiceSchedulerJob.getRegexFromFilter(), equalTo("regex from filter"));
		assertThat(emailServiceSchedulerJob.getRegexSubjectFilter(), equalTo("regex subject filter"));
		assertThat(emailServiceSchedulerJob.isAttachmentsActive(), equalTo(true));
		assertThat(emailServiceSchedulerJob.isWorkflowActive(), equalTo(true));
		assertThat(emailServiceSchedulerJob.getWorkflowClassName(), equalTo("workflow class name"));
		assertThat(emailServiceSchedulerJob.getWorkflowFieldsMapping(), equalTo("workflow fields mapping"));
		assertThat(emailServiceSchedulerJob.isWorkflowAdvanceable(), equalTo(true));
		assertThat(emailServiceSchedulerJob.isAttachmentsStorableToWorkflow(), equalTo(true));
	}

	@Test
	public void emailServiceSchedulerJobUpdated() throws Exception {
		// given
		final EmailServiceSchedulerJob updatedOne = new EmailServiceSchedulerJob(42L) {
			{
				setDescription("new description");
				setRunning(true);
				setCronExpression("new cron expression");
				setEmailAccount("new email account");
				setNotificationActive(false);
				setRegexFromFilter("new regex from filter");
				setRegexSubjectFilter("new regex subject filter");
				setAttachmentsActive(false);
				setWorkflowActive(false);
				setWorkflowClassName("new workflow class name");
				setWorkflowFieldsMapping("new workflow fields mapping");
				setWorkflowAdvanceable(false);
				setAttachmentsStorableToWorkflow(false);
			}
		};
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.ACCOUNT_NAME) //
								.withValue("account name") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_NOTIFICATION_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.FILTER_FROM_REGEX) //
								.withValue("regex from filter") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.FILTER_SUBJECT_REGEX) //
								.withValue("regex subject filter") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_ATTACHMENTS_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ACTIVE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_CLASS_NAME) //
								.withValue("workflow class name") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING) //
								.withValue("workflow fields mapping") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ADVANCE) //
								.withValue("true") //
								.build(), //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE) //
								.withValue("true") //
								.build() //
						));

		// when
		store.update(updatedOne);

		// then
		final ArgumentCaptor<Groupable> groupableCaptor = ArgumentCaptor.forClass(Groupable.class);
		final ArgumentCaptor<SchedulerJobParameter> updatedParameterCaptor = ArgumentCaptor
				.forClass(SchedulerJobParameter.class);
		final InOrder inOrder = inOrder(schedulerJobStore, schedulerJobParameterStore);
		inOrder.verify(schedulerJobStore).update(updatedOne);
		inOrder.verify(schedulerJobParameterStore).list(groupableCaptor.capture());
		inOrder.verify(schedulerJobParameterStore, atLeastOnce()).update(updatedParameterCaptor.capture());
		inOrder.verifyNoMoreInteractions();

		final Groupable capturedGroupable = groupableCaptor.getValue();
		assertThat(capturedGroupable.getGroupAttributeName(), equalTo(SCHEDULER_ID));
		assertThat(capturedGroupable.getGroupAttributeValue(), equalTo((Object) updatedOne.getIdentifier()));

		final Map<String, SchedulerJobParameter> parametersByKey = uniqueIndex(updatedParameterCaptor.getAllValues(),
				BY_KEY);

		assertParameter(parametersByKey, updatedOne, ReadEmail.ACCOUNT_NAME, "new email account");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_NOTIFICATION_ACTIVE, "false");
		assertParameter(parametersByKey, updatedOne, ReadEmail.FILTER_FROM_REGEX, "new regex from filter");
		assertParameter(parametersByKey, updatedOne, ReadEmail.FILTER_SUBJECT_REGEX, "new regex subject filter");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_ATTACHMENTS_ACTIVE, "false");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_WORKFLOW_ACTIVE, "false");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_WORKFLOW_CLASS_NAME, "new workflow class name");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING,
				"new workflow fields mapping");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_WORKFLOW_ADVANCE, "false");
		assertParameter(parametersByKey, updatedOne, ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE, "false");
	}

	@Test
	public void emailServiceSchedulerJobDeleted() throws Exception {
		// given
		final EmailServiceSchedulerJob existingOne = new EmailServiceSchedulerJob(42L);
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
	public void emailServiceSchedulerJobListed() throws Exception {
		// given
		final SchedulerJob existingOne = new EmailServiceSchedulerJob(42L);
		when(schedulerJobStore.list()) //
				.thenReturn(asList(existingOne));
		when(schedulerJobParameterStore.list(any(Groupable.class))) //
				.thenReturn(asList( //
						SchedulerJobParameter.newInstance() //
								.withId(123L) //
								.withOwner(42L) //
								.withKey(ReadEmail.ACCOUNT_NAME) //
								.withValue("account name") //
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

		assertThat(element, instanceOf(EmailServiceSchedulerJob.class));
		final EmailServiceSchedulerJob emailServiceSchedulerJob = EmailServiceSchedulerJob.class.cast(element);
		assertThat(emailServiceSchedulerJob.getId(), equalTo(42L));
		assertThat(emailServiceSchedulerJob.getEmailAccount(), equalTo("account name"));
	}

	/*
	 * Utilities
	 */

	private void assertParameter(final Map<String, SchedulerJobParameter> parametersByKey,
			final EmailServiceSchedulerJob owner, final String key, final String expected) {
		final SchedulerJobParameter accountName = parametersByKey.get(key);
		assertThat(accountName, not(nullValue(SchedulerJobParameter.class)));
		assertThat(accountName.getOwner(), equalTo(owner.getId()));
		assertThat(accountName.getValue(), equalTo(expected));
	}

}
