package unit.logic.taskmanager;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.ReadEmail;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.StartWorkflow;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.SynchronousEvent;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

public class DefaultLogicAndStoreConverterTest {

	private DefaultLogicAndStoreConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndStoreConverter();
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final ReadEmailTask source = ReadEmailTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withEmailAccount("email account") //
				.withRegexFromFilter("regex from filter") //
				.withRegexSubjectFilter("regex subject filter") //
				.withNotificationStatus(true) //
				.withAttachmentsRuleActive(true) //
				.withWorkflowRuleActive(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowFieldsMapping("workflow fields mapping") //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(true) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ReadEmailTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.ACCOUNT_NAME, "email account"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_FROM_REGEX, "regex from filter"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_SUBJECT_REGEX, "regex subject filter"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_NOTIFICATION_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_ATTACHMENTS_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING, "workflow fields mapping"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_WORKFLOW_ADVANCE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE, "true"));
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance().withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(ReadEmail.ACCOUNT_NAME, "email account") //
				.withParameter(ReadEmail.FILTER_FROM_REGEX, "regex from filter") //
				.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, "regex subject filter") //
				.withParameter(ReadEmail.RULE_NOTIFICATION_ACTIVE, "true") //
				.withParameter(ReadEmail.RULE_ATTACHMENTS_ACTIVE, "true") //
				.withParameter(ReadEmail.RULE_WORKFLOW_ACTIVE, "true") //
				.withParameter(ReadEmail.RULE_WORKFLOW_CLASS_NAME, "workflow class name") //
				.withParameter(ReadEmail.RULE_WORKFLOW_FIELDS_MAPPING, "workflow fields mapping") //
				.withParameter(ReadEmail.RULE_WORKFLOW_ADVANCE, "true") //
				.withParameter(ReadEmail.RULE_WORKFLOW_ATTACHMENTS_SAVE, "true") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getEmailAccount(), equalTo("email account"));
		assertThat(converted.isNotificationRuleActive(), equalTo(true));
		assertThat(converted.getRegexFromFilter(), equalTo("regex from filter"));
		assertThat(converted.getRegexSubjectFilter(), equalTo("regex subject filter"));
		assertThat(converted.isAttachmentsRuleActive(), equalTo(true));
		assertThat(converted.isWorkflowRuleActive(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		assertThat(converted.getWorkflowFieldsMapping(), equalTo("workflow fields mapping"));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isWorkflowAttachments(), equalTo(true));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final StartWorkflowTask source = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withProcessClass("class name") //
				.withAttributes(attributes) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.StartWorkflowTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(StartWorkflow.CLASSNAME, "class name"));
		assertThat(parameters, hasEntry(StartWorkflow.ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.join(attributes)));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask source = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, "foo=bar\nbar=baz\nbaz=foo") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getAttributes(), equalTo(attributes));
	}

	@Test
	public void startWorkflowTaskWithEmptyAttributesSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask source = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, EMPTY) //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		assertThat(converted.getAttributes().isEmpty(), is(true));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask source = SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withPhase(Phase.AFTER_CREATE) //
				.withScriptingEnableStatus(true) //
				.withScriptingEngine("groovy") //
				.withScript("blah blah blah") //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.SynchronousEventTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(SynchronousEvent.PHASE, "afterCreate"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah"));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask afterCreate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_CREATE) //
				.build();
		final SynchronousEventTask beforeUpdate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_UPDATE) //
				.build();
		final SynchronousEventTask afterUpdate = SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_UPDATE) //
				.build();
		final SynchronousEventTask beforeDelete = SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_DELETE) //
				.build();

		// when
		final org.cmdbuild.data.store.task.Task convertedAfterCreate = converter.from(afterCreate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeUpdate = converter.from(beforeUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedAfterUpdate = converter.from(afterUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeDelete = converter.from(beforeDelete).toStore();

		// then
		assertThat(convertedAfterCreate.getParameters(), hasEntry(SynchronousEvent.PHASE, "afterCreate"));
		assertThat(convertedBeforeUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "beforeUpdate"));
		assertThat(convertedAfterUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "afterUpdate"));
		assertThat(convertedBeforeDelete.getParameters(), hasEntry(SynchronousEvent.PHASE, "beforeDelete"));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask source = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withParameter(SynchronousEvent.PHASE, "afterCreate") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah") //
				.build();

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(SynchronousEventTask.class));
		final SynchronousEventTask converted = SynchronousEventTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(converted.isScriptingEnabled(), equalTo(true));
		assertThat(converted.getScriptingEngine(), equalTo("groovy"));
		assertThat(converted.getScriptingScript(), equalTo("blah blah blah"));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask afterCreate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "afterCreate") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeUpdate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "beforeUpdate") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask afterUpdate = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "afterUpdate") //
				.build();
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeDelete = org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "beforeDelete") //
				.build();

		// when
		final SynchronousEventTask convertedAfterCreate = SynchronousEventTask.class.cast(converter.from(afterCreate)
				.toLogic());
		final SynchronousEventTask convertedBeforeUpdate = SynchronousEventTask.class.cast(converter.from(beforeUpdate)
				.toLogic());
		final SynchronousEventTask convertedAfterUpdate = SynchronousEventTask.class.cast(converter.from(afterUpdate)
				.toLogic());
		final SynchronousEventTask convertedBeforeDelete = SynchronousEventTask.class.cast(converter.from(beforeDelete)
				.toLogic());

		// then
		assertThat(convertedAfterCreate.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(convertedBeforeUpdate.getPhase(), equalTo(Phase.BEFORE_UPDATE));
		assertThat(convertedAfterUpdate.getPhase(), equalTo(Phase.AFTER_UPDATE));
		assertThat(convertedBeforeDelete.getPhase(), equalTo(Phase.BEFORE_DELETE));
	}

}
