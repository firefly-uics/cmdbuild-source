package org.cmdbuild.servlets.json.schema.taskmanager;

import static com.google.common.collect.FluentIterable.from;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.NULL_SOURCE_CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.CommunicationConstants.ATTRIBUTE_MAPPING;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.CRON_EXPRESSION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_CONFIGURATION;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_ADDRESS;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_FILTER;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_NAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_PASSWORD;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_PORT;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_DB_USERNAME;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_TYPE;
import static org.cmdbuild.servlets.json.CommunicationConstants.DATA_SOURCE_TYPE_SQL;
import static org.cmdbuild.servlets.json.CommunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.CommunicationConstants.ID;
import static org.cmdbuild.servlets.json.CommunicationConstants.IS_KEY;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_ATTRIBUTE;
import static org.cmdbuild.servlets.json.CommunicationConstants.SOURCE_NAME;
import static org.cmdbuild.servlets.json.schema.TaskManager.TASK_TO_JSON_TASK;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.cmdbuild.logic.taskmanager.ConnectorTask;
import org.cmdbuild.logic.taskmanager.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfiguration;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.Task;
import org.cmdbuild.services.json.dto.JsonResponse;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.json.schema.TaskManager.JsonElements;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.google.common.base.Function;

public class Connector extends JSONBaseWithSpringContext {

	private static abstract class JsonDataSource {
	}

	private static class JsonSqlDataSource extends JsonDataSource {

		private final SqlSourceConfiguration delegate;

		public JsonSqlDataSource(final SqlSourceConfiguration delegate) {
			this.delegate = delegate;
		}

		@JsonProperty(DATA_SOURCE_DB_ADDRESS)
		public String getHost() {
			return delegate.getHost();
		}

		@JsonProperty(DATA_SOURCE_DB_PORT)
		public int getPort() {
			return delegate.getPort();
		}

		@JsonProperty(DATA_SOURCE_DB_NAME)
		public String getDatabase() {
			return delegate.getDatabase();
		}

		@JsonProperty(DATA_SOURCE_DB_USERNAME)
		public String getUsername() {
			return delegate.getUsername();
		}

		@JsonProperty(DATA_SOURCE_DB_PASSWORD)
		public String getPassword() {
			return delegate.getPassword();
		}

		@JsonProperty(DATA_SOURCE_DB_FILTER)
		public String getFilter() {
			return delegate.getFilter();
		}

	}

	private static class JsonAttributeMapping {

		private String sourceType;
		private String sourceAttribute;
		private String targetType;
		private String targetAttribute;
		private Boolean isKey;

		@JsonProperty(SOURCE_NAME)
		public String getSourceType() {
			return sourceType;
		}

		public void setSourceType(final String sourceType) {
			this.sourceType = sourceType;
		}

		@JsonProperty(SOURCE_ATTRIBUTE)
		public String getSourceAttribute() {
			return sourceAttribute;
		}

		public void setSourceAttribute(final String sourceAttribute) {
			this.sourceAttribute = sourceAttribute;
		}

		@JsonProperty(CLASS_NAME)
		public String getTargetType() {
			return targetType;
		}

		public void setTargetType(final String targetType) {
			this.targetType = targetType;
		}

		@JsonProperty(CLASS_ATTRIBUTE)
		public String getTargetAttribute() {
			return targetAttribute;
		}

		public void setTargetAttribute(final String targetAttribute) {
			this.targetAttribute = targetAttribute;
		}

		@JsonProperty(IS_KEY)
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

	private static final Function<AttributeMapping, JsonAttributeMapping> ATTRIBUTE_MAPPING_TO_JSON_ATTRIBUTE_MAPPING = new Function<AttributeMapping, JsonAttributeMapping>() {

		@Override
		public JsonAttributeMapping apply(final AttributeMapping input) {
			final JsonAttributeMapping output = new JsonAttributeMapping();
			output.setSourceType(input.getSourceType());
			output.setSourceAttribute(input.getSourceAttribute());
			output.setTargetType(input.getTargetType());
			output.setTargetAttribute(input.getTargetAttribute());
			output.setIsKey(input.isKey());
			return output;
		}

	};

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

		@JsonProperty(DATA_SOURCE_TYPE)
		public String getSourceType() {
			return new SourceConfigurationVisitor() {

				private String type;

				public String asJsonObject() {
					delegate.getSourceConfiguration().accept(this);
					return type;
				}

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					type = DATA_SOURCE_TYPE_SQL;
				}

			}.asJsonObject();
		}

		@JsonProperty(DATA_SOURCE_CONFIGURATION)
		public JsonDataSource getSourceConfiguration() {
			return new SourceConfigurationVisitor() {

				private JsonDataSource jsonDataSource;

				public JsonDataSource asJsonObject() {
					delegate.getSourceConfiguration().accept(this);
					return jsonDataSource;
				}

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					jsonDataSource = new JsonSqlDataSource(sourceConfiguration);
				}

			}.asJsonObject();
		}

		@JsonProperty(ATTRIBUTE_MAPPING)
		public List<JsonAttributeMapping> getAttributeMapping() {
			return from(delegate.getAttributeMappings()) //
					.transform(ATTRIBUTE_MAPPING_TO_JSON_ATTRIBUTE_MAPPING) //
					.toList();
		}

	}

	private static final Iterable<JsonAttributeMapping> NO_MAPPINGS = Collections.emptyList();

	private static final TypeReference<Set<? extends JsonAttributeMapping>> JSON_ATTRIBUTE_MAPPINGS_TYPE_REFERENCE = new TypeReference<Set<? extends JsonAttributeMapping>>() {
	};

	@Admin
	@JSONExported
	public JsonResponse create( //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = DATA_SOURCE_TYPE, required = false) final String dataSourceType, //
			@Parameter(value = DATA_SOURCE_CONFIGURATION, required = false) final String jsonDataSourceConfiguration, //
			@Parameter(value = ATTRIBUTE_MAPPING, required = false) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withSourceConfiguration(sourceConfigurationOf(dataSourceType, jsonDataSourceConfiguration)) //
				.withAttributeMappings(attributeMappingOf(jsonAttributeMapping)) //
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
	public JsonResponse update( // //
			@Parameter(ID) final Long id, //
			@Parameter(DESCRIPTION) final String description, //
			@Parameter(ACTIVE) final Boolean active, //
			@Parameter(CRON_EXPRESSION) final String cronExpression, //
			@Parameter(value = DATA_SOURCE_TYPE, required = false) final String dataSourceType, //
			@Parameter(value = DATA_SOURCE_CONFIGURATION, required = false) final String jsonDataSourceConfiguration, //
			@Parameter(value = ATTRIBUTE_MAPPING, required = false) final String jsonAttributeMapping //
	) throws Exception {
		final ConnectorTask task = ConnectorTask.newInstance() //
				.withId(id) //
				.withDescription(description) //
				.withActiveStatus(active) //
				.withCronExpression(cronExpression) //
				.withSourceConfiguration(sourceConfigurationOf(dataSourceType, jsonDataSourceConfiguration)) //
				.withAttributeMappings(attributeMappingOf(jsonAttributeMapping)) //
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

	private static enum JsonSourceConfigurationHandler {

		SQL(DATA_SOURCE_TYPE_SQL) {

			@Override
			public SourceConfiguration convert(final String json) throws Exception {
				final JsonNode jsonNode = objectMapper.readTree(json);
				return SqlSourceConfiguration.newInstance() //
						.withHost(jsonNode.get(DATA_SOURCE_DB_ADDRESS).asText()) //
						.withPort(jsonNode.get(DATA_SOURCE_DB_PORT).asInt()) //
						.withDatabase(jsonNode.get(DATA_SOURCE_DB_NAME).asText()) //
						.withUsername(jsonNode.get(DATA_SOURCE_DB_USERNAME).asText()) //
						.withPassword(jsonNode.get(DATA_SOURCE_DB_PASSWORD).asText()) //
						.withFilter(jsonNode.get(DATA_SOURCE_DB_FILTER).asText()) //
						.build();
			}

		}, //
		UNDEFINED(null) {

			@Override
			public SourceConfiguration convert(final String json) throws Exception {
				return NULL_SOURCE_CONFIGURATION;
			}

		}, //
		;

		private static final ObjectMapper objectMapper = new ObjectMapper();

		public static JsonSourceConfigurationHandler of(final String type) {
			JsonSourceConfigurationHandler found = null;
			for (final JsonSourceConfigurationHandler element : values()) {
				if (ObjectUtils.equals(element.clientValue, type)) {
					found = element;
					break;
				}
			}
			Validate.notNull(found, "type '%s' not found", type);
			return found;
		}

		private final String clientValue;

		private JsonSourceConfigurationHandler(final String clientValue) {
			this.clientValue = clientValue;
		}

		public abstract SourceConfiguration convert(String json) throws Exception;

	}

	private SourceConfiguration sourceConfigurationOf(final String type, final String jsonConfiguration)
			throws Exception {
		return JsonSourceConfigurationHandler.of(type).convert(jsonConfiguration);
	}

	private Iterable<AttributeMapping> attributeMappingOf(final String json) throws JsonParseException,
			JsonMappingException, IOException {
		final Iterable<JsonAttributeMapping> jsonAttributeMappings;
		if (isBlank(json)) {
			jsonAttributeMappings = NO_MAPPINGS;
		} else {
			jsonAttributeMappings = new ObjectMapper() //
					.readValue(json, JSON_ATTRIBUTE_MAPPINGS_TYPE_REFERENCE);
		}
		return from(jsonAttributeMappings) //
				.transform(JSON_ATTRIBUTE_MAPPING_TO_ATTRIBUTE_MAPPING);
	}

}
