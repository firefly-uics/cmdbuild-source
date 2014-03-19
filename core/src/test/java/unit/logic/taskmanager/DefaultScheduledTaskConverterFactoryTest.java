package unit.logic.taskmanager;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.cmdbuild.logic.taskmanager.DefaultScheduledTaskConverterFactory;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.ScheduledTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Maps;

public class DefaultScheduledTaskConverterFactoryTest {

	private DefaultScheduledTaskConverterFactory converterFactory;

	@Before
	public void setUp() throws Exception {
		converterFactory = new DefaultScheduledTaskConverterFactory();
	}

	@Test
	public void readEmailTaskSuccessfullyConverted() throws Exception {
		// given
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(42L) //
				.withDescription("the description") //
				.withActiveStatus(true) //
				.withCronExpression("the cron expression") //
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
		final SchedulerJob converted = converterFactory.of(task).toSchedulerJob();

		// then
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("the description"));
		assertThat(converted.isRunning(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("the cron expression"));
		assertThat(converted, instanceOf(EmailServiceSchedulerJob.class));

		final EmailServiceSchedulerJob emailServiceSchedulerJob = EmailServiceSchedulerJob.class.cast(converted);
		assertThat(emailServiceSchedulerJob.getEmailAccount(), equalTo("email account"));
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
	public void emailServiceJobSuccessfullyConverted() throws Exception {
		// given
		final EmailServiceSchedulerJob schedulerJob = new EmailServiceSchedulerJob(42L) {
			{
				setDescription("description");
				setRunning(true);
				setCronExpression("cron expression");
				setEmailAccount("email account");
				setNotificationActive(true);
				setRegexFromFilter("regex from filter");
				setRegexSubjectFilter("regex subject filter");
				setAttachmentsActive(true);
				setWorkflowActive(true);
				setWorkflowClassName("workflow class name");
				setWorkflowFieldsMapping("workflow fields mapping");
				setWorkflowAdvanceable(true);
				setAttachmentsStorableToWorkflow(true);
			}
		};

		// when
		final ScheduledTask converted = converterFactory.of(schedulerJob).toScheduledTask();

		// then
		assertThat(converted, instanceOf(ReadEmailTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		final ReadEmailTask convertedReadEmailTask = ReadEmailTask.class.cast(converted);
		assertThat(convertedReadEmailTask.getEmailAccount(), equalTo("email account"));
		assertThat(convertedReadEmailTask.isNotificationRuleActive(), equalTo(true));
		assertThat(convertedReadEmailTask.getRegexFromFilter(), equalTo("regex from filter"));
		assertThat(convertedReadEmailTask.getRegexSubjectFilter(), equalTo("regex subject filter"));
		assertThat(convertedReadEmailTask.isAttachmentsRuleActive(), equalTo(true));
		assertThat(convertedReadEmailTask.isWorkflowRuleActive(), equalTo(true));
		assertThat(convertedReadEmailTask.getWorkflowClassName(), equalTo("workflow class name"));
		assertThat(convertedReadEmailTask.getWorkflowFieldsMapping(), equalTo("workflow fields mapping"));
		assertThat(convertedReadEmailTask.isWorkflowAdvanceable(), equalTo(true));
		assertThat(convertedReadEmailTask.isWorkflowAttachments(), equalTo(true));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConverted() throws Exception {
		// given
		final Map<String, String> parameters = Maps.newHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo\nlol");
		final StartWorkflowTask task = StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("the description") //
				.withActiveStatus(true) //
				.withCronExpression("the cron expression") //
				.withProcessClass("the class name") //
				.withParameters(parameters) //
				.build();

		// when
		final SchedulerJob converted = converterFactory.of(task).toSchedulerJob();

		// then
		assertThat(converted, instanceOf(WorkflowSchedulerJob.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("the description"));
		assertThat(converted.getCronExpression(), equalTo("the cron expression"));
		assertThat(converted.isRunning(), equalTo(true));
		final WorkflowSchedulerJob _converted = WorkflowSchedulerJob.class.cast(converted);
		assertThat(_converted.getProcessClass(), equalTo("the class name"));
		assertThat(_converted.getParameters(), equalTo(parameters));
	}

	@Test
	public void workflowSchedulerJobSuccessfullyConverted() throws Exception {
		// given
		final Map<String, String> parameters = Maps.newHashMap();
		parameters.put("foo", "bar");
		parameters.put("bar", "baz");
		parameters.put("baz", "foo\nlol");
		final WorkflowSchedulerJob schedulerJob = new WorkflowSchedulerJob(42L) {
			{
				setDescription("the description");
				setRunning(true);
				setCronExpression("the cron expression");
				setProcessClass("the class name");
				setParameters(parameters);
			}
		};

		// when
		final ScheduledTask converted = converterFactory.of(schedulerJob).toScheduledTask();

		// then
		assertThat(converted, instanceOf(StartWorkflowTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("the description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("the cron expression"));
		final StartWorkflowTask convertedStartWorkflowTask = StartWorkflowTask.class.cast(converted);
		assertThat(convertedStartWorkflowTask.getProcessClass(), equalTo("the class name"));
		assertThat(convertedStartWorkflowTask.getParameters(), equalTo(parameters));
	}

}
