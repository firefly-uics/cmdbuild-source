package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.GROUPS;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_ACCOUNT;
import static org.cmdbuild.servlets.json.CommunicationConstants.NOTIFICATION_EMAIL_TEMPLATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_AFTER_CREATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_AFTER_UPDATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_BEFORE_DELETE;
import static org.cmdbuild.servlets.json.CommunicationConstants.PHASE_BEFORE_UPDATE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ADVANCEABLE;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_ATTRIBUTES;
import static org.cmdbuild.servlets.json.CommunicationConstants.WORKFLOW_CLASS_NAME;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;
import static org.cmdbuild.servlets.json.schema.Utils.toIterable;
import static org.cmdbuild.servlets.json.schema.Utils.toMap;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONArray;
import org.json.JSONObject;

public class AsynchronousEvent extends JSONBaseWithSpringContext {

	private static class JsonAsynchronousEventTask {

		public JsonAsynchronousEventTask() {
		}

		@JsonProperty(ID)
		public Long getId() {
			return null;
		}

		@JsonProperty(DESCRIPTION)
		public String getDescription() {
			return null;
		}

		@JsonProperty(ACTIVE)
		public boolean isActive() {
			return false;
		}

		@JsonProperty(CRON_EXPRESSION)
		public String getCronExpression() {
			return null;
		}

		@JsonProperty(CLASS_NAME)
		public String getTargetClassname() {
			return null;
		}

		@JsonProperty(FILTER)
		public String getFilter() {
			return null;
		}

		@JsonProperty(NOTIFICATION_ACTIVE)
		public boolean isEmailEnabled() {
			return false;
		}

		@JsonProperty(NOTIFICATION_EMAIL_ACCOUNT)
		public String getEmailAccount() {
			return null;
		}

		@JsonProperty(NOTIFICATION_EMAIL_TEMPLATE)
		public String getEmailTemplate() {
			return null;
		}

	}

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate //
	) {
		// TODO
		return JsonResponse.success(null);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		return JsonResponse.success(new JsonAsynchronousEventTask());
	}

	@JSONExported
	public JsonResponse readAll() {
		// TODO
		final Iterable<? extends Task> tasks = Collections.emptyList();
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
			@Parameter(value = CLASS_NAME, required = false) final String classname, //
			@Parameter(value = FILTER, required = false) final String filter, //
			@Parameter(value = NOTIFICATION_ACTIVE, required = false) final Boolean emailActive, //
			@Parameter(value = NOTIFICATION_EMAIL_ACCOUNT, required = false) final String emailAccount, //
			@Parameter(value = NOTIFICATION_EMAIL_TEMPLATE, required = false) final String emailTemplate //
	) {
		// TODO
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		// TODO
		// taskManagerLogic().delete(...);
	}

}
