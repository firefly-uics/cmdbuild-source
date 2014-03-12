package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.data.store.scheduler.EmailServiceSchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJob;
import org.cmdbuild.data.store.scheduler.SchedulerJobVisitor;
import org.cmdbuild.data.store.scheduler.WorkflowSchedulerJob;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class DefaultScheduledTaskConverterFactory implements ScheduledTaskFacadeConverterFactory {

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultScheduledTaskConverterFactory.class.getName());

	private static class DefaultScheduledTaskConverter implements ScheduledTaskConverter, TaskVistor {

		private final ScheduledTask task;

		private SchedulerJob schedulerJob;

		public DefaultScheduledTaskConverter(final ScheduledTask task) {
			this.task = task;
		}

		@Override
		public SchedulerJob toSchedulerJob() {
			logger.info(marker, "converting task '{}' to scheduler job", task);
			task.accept(this);
			Validate.notNull(schedulerJob, "conversion error");
			return schedulerJob;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			final EmailServiceSchedulerJob schedulerJob = new EmailServiceSchedulerJob(task.getId());
			schedulerJob.setDescription(task.getDescription());
			schedulerJob.setRunning(task.isActive());
			schedulerJob.setCronExpression(task.getCronExpression());
			schedulerJob.setEmailAccount(task.getEmailAccount());
			schedulerJob.setNotificationActive(task.isNotificationRuleActive());
			schedulerJob.setRegexFromFilter(task.getRegexFromFilter());
			schedulerJob.setRegexSubjectFilter(task.getRegexSubjectFilter());
			schedulerJob.setAttachmentsActive(task.isAttachmentsRuleActive());
			schedulerJob.setWorkflowActive(task.isWorkflowRuleActive());
			schedulerJob.setWorkflowClassName(task.getWorkflowClassName());
			schedulerJob.setWorkflowFieldsMapping(task.getWorkflowFieldsMapping());
			schedulerJob.setWorkflowAdvanceable(task.isWorkflowAdvanceable());
			schedulerJob.setAttachmentsStorableToWorkflow(task.isWorkflowAttachments());
			this.schedulerJob = schedulerJob;
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			final WorkflowSchedulerJob schedulerJob = new WorkflowSchedulerJob(task.getId());
			schedulerJob.setDescription(task.getDescription());
			schedulerJob.setRunning(task.isActive());
			schedulerJob.setCronExpression(task.getCronExpression());
			schedulerJob.setProcessClass(task.getProcessClass());
			schedulerJob.setParameters(task.getParameters());
			this.schedulerJob = schedulerJob;
		}

	}

	private static class DefaultSchedulerJobConverter implements SchedulerJobConverter, SchedulerJobVisitor {

		private final SchedulerJob schedulerJob;

		private ScheduledTask scheduledTask;

		public DefaultSchedulerJobConverter(final SchedulerJob schedulerJob) {
			this.schedulerJob = schedulerJob;
		}

		@Override
		public ScheduledTask toScheduledTask() {
			logger.info(marker, "converting scheduler job '{}' to scheduled task");
			schedulerJob.accept(this);
			Validate.notNull(scheduledTask, "conversion error");
			return scheduledTask;
		}

		@Override
		public void visit(final EmailServiceSchedulerJob schedulerJob) {
			scheduledTask = ReadEmailTask.newInstance() //
					.withId(schedulerJob.getId()) //
					.withDescription(schedulerJob.getDescription()) //
					.withActiveStatus(schedulerJob.isRunning()) //
					.withCronExpression(schedulerJob.getCronExpression()) //
					.withEmailAccount(schedulerJob.getEmailAccount()) //
					.withRegexFromFilter(schedulerJob.getRegexFromFilter()) //
					.withRegexSubjectFilter(schedulerJob.getRegexSubjectFilter()) //
					.withNotificationStatus(schedulerJob.isNotificationActive()) //
					.withAttachmentsRuleActive(schedulerJob.isAttachmentsActive()) //
					.withWorkflowRuleActive(schedulerJob.isWorkflowActive()) //
					.withWorkflowClassName(schedulerJob.getWorkflowClassName()) //
					.withWorkflowFieldsMapping(schedulerJob.getWorkflowFieldsMapping()) //
					.withWorkflowAdvanceableStatus(schedulerJob.isWorkflowAdvanceable()) //
					.withWorkflowAttachmentsStatus(schedulerJob.isAttachmentsStorableToWorkflow()) //
					.build();
		}

		@Override
		public void visit(final WorkflowSchedulerJob schedulerJob) {
			scheduledTask = StartWorkflowTask.newInstance() //
					.withId(schedulerJob.getId()) //
					.withDescription(schedulerJob.getDescription()) //
					.withActiveStatus(schedulerJob.isRunning()) //
					.withCronExpression(schedulerJob.getCronExpression()) //
					.withProcessClass(schedulerJob.getProcessClass()) //
					.withParameters(schedulerJob.getParameters()) //
					.build();
		}

	}

	@Override
	public ScheduledTaskConverter of(final ScheduledTask scheduledTask) {
		return new DefaultScheduledTaskConverter(scheduledTask);
	}

	@Override
	public SchedulerJobConverter of(final SchedulerJob schedulerJob) {
		return new DefaultSchedulerJobConverter(schedulerJob);
	}

}
