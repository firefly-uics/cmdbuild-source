package unit.logic.taskmanager;

import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.ReadEmail;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.StartWorkflow;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
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
	public void emailServiceJobSuccessfullyConverted() throws Exception {
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
		final Map<String, String> _parameters = Maps.newHashMap();
		_parameters.put("foo", "bar");
		_parameters.put("bar", "baz");
		_parameters.put("baz", "foo");
		final StartWorkflowTask source = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withProcessClass("class name") //
				.withParameters(_parameters) //
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
				.join(_parameters)));
	}

	@Test
	public void workflowSchedulerJobSuccessfullyConverted() throws Exception {
		// given
		final org.cmdbuild.data.store.task.StartWorkflowTask schedulerJob = org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, "foo=bar\nbar=baz\nbaz=foo") //
				.build();

		// when
		final Task _converted = converter.from(schedulerJob).toLogic();

		// then
		assertThat(_converted, instanceOf(StartWorkflowTask.class));
		final StartWorkflowTask converted = StartWorkflowTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.getProcessClass(), equalTo("class name"));
		final Map<String, String> parameters = Maps.newHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo");
		assertThat(converted.getParameters(), equalTo(parameters));
	}

}
