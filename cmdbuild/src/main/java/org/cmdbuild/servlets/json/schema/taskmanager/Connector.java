package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.ATTRIBUTE_MAPPING;
import static org.cmdbuild.servlets.json.ComunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.data.store.task.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Function;

public class Connector extends JSONBaseWithSpringContext {

	public static class JsonAttributeMapping {

		private String sourceType;
		private String sourceAttribute;
		private String targetType;
		private String targetAttribute;
		private Boolean isKey;

		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(final String sourceType) {
			this.sourceType = sourceType;
		}

		public String getSourceAttribute() {
			return sourceAttribute;
		}

		public void setSourceAttribute(final String sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
		}

		public String getTargetType() {
			return targetType;
		}

		public void setTargetType(final String targetType) {
			this.targetType = targetType;
		}

		public String getTargetAttribute() {
			return targetAttribute;
		}

		public void setTargetAttribute(final String targetAttribute) {
			this.targetAttribute = targetAttribute;
		}

		public boolean isKey() {
			return defaultIfNull(isKey, false);
		}

		public void setIsKey(final boolean isKey) {
			this.isKey = isKey;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof JsonAttributeMapping)) {
				return false;
			}
			final JsonAttributeMapping other = JsonAttributeMapping.class.cast(obj);
			return new EqualsBuilder() //
					.append(sourceType, other.sourceType) //
					.append(sourceAttribute, other.sourceAttribute) //
					.append(targetType, other.targetType) //
					.append(targetAttribute, other.targetAttribute) //
					.isEquals();
		}

		@Override
		public int hashCode() {
			return new HashCodeBuilder() //
					.append(sourceType) //
					.append(sourceAttribute) //
					.append(targetType) //
					.append(targetAttribute) //
					.toHashCode();
		}

	}

	private static class JsonConnectorTask {

		private final ConnectorTask delegate;

		public JsonConnectorTask(final ConnectorTask delegate) {
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

	}

	private static final Function<JsonAttributeMapping, AttributeMapping> JSON_ATTRIBUTE_MAPPING_TO_ATTRIBUTE_MAPPING = new Function<JsonAttributeMapping, AttributeMapping>() {

		@Override
		public AttributeMapping apply(final JsonAttributeMapping input) {
			return AttributeMapping.newInstance() //
					.withSourceType(input.getSourceType()) //
					.withSourceAttribute(input.getSourceAttribute()) //
					.withTargetType(input.getTargetType()) //
					.withTargetAttribute(input.getTargetAttribute()) //
					.withKeyStatus(input.isKey()) //
					.build();
		}

	};

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(ATTRIBUTE_MAPPING) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withAttributeMapping(attributeMappingOf(jsonAttributeMapping)) //
				.build();
		final Long id = taskManagerLogic().create(task);
		return JsonResponse.success(id);
	}

	@JSONExported
	public JsonResponse read( //
			@Parameter(value = ID) final Long id //
	) {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.build();
		final ConnectorTask readed = taskManagerLogic().read(task, ConnectorTask.class);
		return JsonResponse.success(new JsonConnectorTask(readed));
	}

	@JSONExported
	public JsonResponse readAll() {
		final Iterable<? extends Task> tasks = taskManagerLogic().read(ConnectorTask.class);
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
			@Parameter(ATTRIBUTE_MAPPING) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withAttributeMapping(attributeMappingOf(jsonAttributeMapping)) //
				.build();
		taskManagerLogic().update(task);
		return JsonResponse.success();
	}

	@Admin
	@JSONExported
	public void delete( //
			@Parameter(ID) final Long id //
	) {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.build();
		taskManagerLogic().delete(task);
	}

	/*
	 * Utilities
	 */

	private Iterable<AttributeMapping> attributeMappingOf(final String json) throws JsonParseException,
			JsonMappingException, IOException {
		final Iterable<JsonAttributeMapping> jsonAttributeMappings = new ObjectMapper() //
				.readValue( //
						json, //
						new TypeReference<Set<JsonAttributeMapping>>() {
						});
		return from(jsonAttributeMappings) //
				.transform(JSON_ATTRIBUTE_MAPPING_TO_ATTRIBUTE_MAPPING);
	}

}
