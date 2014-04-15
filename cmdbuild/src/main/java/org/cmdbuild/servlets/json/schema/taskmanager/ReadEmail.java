package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTACHMENTS_CATEGORY;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.ComunicationConstants.EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.*;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.ComunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER_FROM_ADDRESS;
import static org.cmdbuild.servlets.json.ComunicationConstants.FILTER_SUBJECT;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.taskmanager.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.taskmanager.Utils.toMap;

import java.util.Map;

import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class ReadEmail extends JSONBaseWithSpringContext {

	private static class JsonReadEmailTask {

		private final ReadEmailTask delegate;

		public JsonReadEmailTask(final ReadEmailTask delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(ID)
		public Long getId() {
			return delegate.getId();
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return delegate.getDescription();
		}

		@JsonProperty(ACTIVE)
		public boolean isActive() {
			return delegate.isActive();
		}

		@JsonProperty(CRON_EXPRESSION)
		public String getCronExpression() {
			return delegate.getCronExpression();
		}

		@JsonProperty(FILTER_FROM_ADDRESS)
		// TODO send array as string?
		public Iterable<String> getRegexFromFilter() {
			return delegate.getRegexFromFilter();
		}

		@JsonProperty(FILTER_SUBJECT)
		// TODO send array as string?
		public Iterable<String> getRegexSubjectFilter() {
			return delegate.getRegexSubjectFilter();
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isNotificationActive() {
			return delegate.isNotificationActive();
		}

		@JsonProperty(ATTACHMENTS_ACTIVE)
		public boolean isAttachmentsActive() {
			return delegate.isAttachmentsActive();
		}

		@JsonProperty(ATTACHMENTS_CATEGORY)
		public String getAttachmentsCategory() {
			return delegate.getAttachmentsCategory();
		}

		@JsonProperty(WORKFLOW_ACTIVE)
		public boolean isWorkflowActive() {
			return delegate.isWorkflowActive();
		}

		@JsonProperty(WORKFLOW_CLASS_NAME)
		public String getWorkflowClassName() {
			return delegate.getWorkflowClassName();
		}

		@JsonProperty(WORKFLOW_ATTRIBUTES)
		// TODO send object as string?
		public Map<String, String> getWorkflowAttributes() {
			return delegate.getWorkflowAttributes();
		}

		@JsonProperty(WORKFLOW_ADVANCEABLE)
		public boolean isWorkflowAdvanceable() {
			return delegate.isWorkflowAdvanceable();
		}

		@JsonProperty(WORKFLOW_SAVE_ATTACHMENTS)
		public boolean isWorkflowAttachments() {
			return delegate.isWorkflowAttachments();
		}

		@JsonProperty(WORKFLOW_ATTACHMENTS_CATEGORY)
		public String getWorkflowAttachmentsCategory() {
			return delegate.getWorkflowAttachmentsCategory();
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final String attachmentsCategory, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategory //
	// TODO mapping
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				//
				// filters
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				//
				// send notification
				// TODO not necessary
				.withNotificationStatus(isNotBlank(emailTemplate)) //
				//
				// store attachments
				.withAttachmentsActive(isNotBlank(attachmentsCategory)) //
				.withAttachmentsCategory(attachmentsCategory) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(isNotBlank(workflowClassName)) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAttachmentsStatus(isNotBlank(workflowAttachmentsCategory)) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategory) //
				//
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		final ReadEmailTask readed = taskManagerLogic().read(task, ReadEmailTask.class);
		return JsonResponse.success(new JsonReadEmailTask(readed));
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(ReadEmailTask.class);
		return JsonResponse.success(JsonElements.of(from(tasks) //
				.transform(TASK_TO_JSON_TASK)));
	}

	@Admin
	@JSONExported
	public JsonResponse update( //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = FILTER_FROM_ADDRESS, required = false) final JSONArray filterFromAddress, //
			@Parameter(value = FILTER_SUBJECT, required = false) final JSONArray filterSubject, //
			@Parameter(value = EMAIL_TEMPLATE, required = false) final String emailTemplate, //
			@Parameter(value = ATTACHMENTS_CATEGORY, required = false) final String attachmentsCategory, //
			@Parameter(value = WORKFLOW_CLASS_NAME, required = false) final String workflowClassName, //
			@Parameter(value = WORKFLOW_ATTRIBUTES, required = false) final JSONObject workflowAttributes, //
			@Parameter(value = WORKFLOW_ATTACHMENTS_CATEGORY, required = false) final String workflowAttachmentsCategory //
	// TODO mapping
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				//
				.withEmailAccount(emailAccount) //
				//
				// filters
				.withRegexFromFilter(toIterable(filterFromAddress)) //
				.withRegexSubjectFilter(toIterable(filterSubject)) //
				//
				// send notification
				// TODO not necessary
				.withNotificationStatus(isNotBlank(emailTemplate)) //
				//
				// store attachments
				.withAttachmentsActive(isNotBlank(attachmentsCategory)) //
				.withAttachmentsCategory(attachmentsCategory) //
				//
				// workflow (start process and, maybe, store attachments)
				.withWorkflowActive(isNotBlank(workflowClassName)) //
				.withWorkflowClassName(workflowClassName) //
				.withWorkflowAttributes(toMap(workflowAttributes)) //
				.withWorkflowAttachmentsStatus(isNotBlank(workflowAttachmentsCategory)) //
				.withWorkflowAttachmentsCategory(workflowAttachmentsCategory) //
				//
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final ReadEmailTask task = ReadEmailTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

}
