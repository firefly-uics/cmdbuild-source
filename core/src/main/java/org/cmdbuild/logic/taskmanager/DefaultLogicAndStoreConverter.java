package org.cmdbuild.logic.taskmanager;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.apache.commons.lang3.math.NumberUtils.createInteger;
import static org.cmdbuild.common.java.sql.DataSourceTypes.mysql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.oracle;
import static org.cmdbuild.common.java.sql.DataSourceTypes.postgresql;
import static org.cmdbuild.common.java.sql.DataSourceTypes.sqlserver;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.NULL_SOURCE_CONFIGURATION;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.java.sql.DataSourceTypes.DataSourceType;
import org.cmdbuild.data.store.task.ConnectorTaskDefinition;
import org.cmdbuild.data.store.task.ReadEmailTaskDefinition;
import org.cmdbuild.data.store.task.StartWorkflowTaskDefinition;
import org.cmdbuild.data.store.task.SynchronousEventTaskDefinition;
import org.cmdbuild.data.store.task.TaskVisitor;
import org.cmdbuild.logic.taskmanager.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask.ClassMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfiguration;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfigurationVisitor;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;

public class DefaultLogicAndStoreConverter implements LogicAndStoreConverter {

	/**
	 * Container for all {@link ConnectorTaskDefinition} parameter names.
	 */
	public static class Connector {

		private Connector() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = "connector.";

		private static final String DATA_SOURCE_PREFIX = ALL_PREFIX + "datasource.";
		public static final String DATA_SOURCE_TYPE = DATA_SOURCE_PREFIX + "type";
		public static final String DATA_SOURCE_CONFIGURATION = DATA_SOURCE_PREFIX + "configuration";

		private static final String SQL_PREFIX = EMPTY;
		public static final String SQL_TYPE = SQL_PREFIX + "type";
		public static final String SQL_HOSTNAME = SQL_PREFIX + "hostname";
		public static final String SQL_PORT = SQL_PREFIX + "port";
		public static final String SQL_DATABASE = SQL_PREFIX + "database";
		public static final String SQL_INSTANCE = SQL_PREFIX + "instance";
		public static final String SQL_USERNAME = SQL_PREFIX + "username";
		public static final String SQL_PASSWORD = SQL_PREFIX + "password";
		public static final String SQL_FILTER = SQL_PREFIX + "filter";

		private static final String NOTIFICATION_PREFIX = ALL_PREFIX + "notification.";
		public static final String NOTIFICATION_ACTIVE = NOTIFICATION_PREFIX + "active";
		public static final String NOTIFICATION_ACCOUNT = NOTIFICATION_PREFIX + "account";
		private static final String NOTIFICATION_TEMPLATE = NOTIFICATION_PREFIX + "template.";
		public static final String NOTIFICATION_ERROR_TEMPLATE = NOTIFICATION_TEMPLATE + "error";

		private static final String MAPPING_PREFIX = ALL_PREFIX + "mapping.";
		public static final String MAPPING_TYPES = MAPPING_PREFIX + "types";
		public static final String MAPPING_ATTRIBUTES = MAPPING_PREFIX + "attributes";

		private static final String MAPPING_SEPARATOR = ",";

	}

	/**
	 * Container for all {@link ReadEmailTaskDefinition} parameter names.
	 */
	public static class ReadEmail {

		private ReadEmail() {
			// prevents instantiation
		}

		private static final String ALL_PREFIX = "email.";

		public static final String ACCOUNT_NAME = ALL_PREFIX + "account.name";

		private static final String FILTER_PREFIX = ALL_PREFIX + "filter.";
		public static final String FILTER_FROM_REGEX = FILTER_PREFIX + "from.regex";
		public static final String FILTER_SUBJECT_REGEX = FILTER_PREFIX + "subject.regex";

		private static final String RULE_PREFIX = ALL_PREFIX + "rule.";

		private static final String ATTACHMENTS_PREFIX = RULE_PREFIX + "attachments.";
		public static final String ATTACHMENTS_ACTIVE = ATTACHMENTS_PREFIX + "active";
		public static final String ATTACHMENTS_CATEGORY = ATTACHMENTS_PREFIX + "category";

		private static final String NOTIFICATION_PREFIX = RULE_PREFIX + "notification.";
		public static final String NOTIFICATION_ACTIVE = NOTIFICATION_PREFIX + "active";
		public static final String NOTIFICATION_TEMPLATE = NOTIFICATION_PREFIX + "template";

		private static final String WORKFLOW_PREFIX = RULE_PREFIX + "workflow.";
		public static final String WORKFLOW_ACTIVE = WORKFLOW_PREFIX + "active";
		public static final String WORKFLOW_ADVANCE = WORKFLOW_PREFIX + "advance";
		public static final String WORKFLOW_CLASS_NAME = WORKFLOW_PREFIX + "class.name";
		public static final String WORKFLOW_FIELDS_MAPPING = WORKFLOW_PREFIX + "fields.mapping";
		private static final String WORKFLOW_ATTACHMENTS_PREFIX = WORKFLOW_PREFIX + "attachments";
		public static final String WORKFLOW_ATTACHMENTS_SAVE = WORKFLOW_ATTACHMENTS_PREFIX + "save";
		public static final String WORKFLOW_ATTACHMENTS_CATEGORY = WORKFLOW_ATTACHMENTS_PREFIX + "category";

		/**
		 * Container for all mapper parameter names.
		 */
		private abstract static class MapperEngine {

			protected static final String ALL_PREFIX = "mapper.";

			public static final String TYPE = ALL_PREFIX + "type";

		}

		/**
		 * Container for all {@link _KeyValueMapperEngine} parameter names.
		 */
		public static class KeyValueMapperEngine extends MapperEngine {

			private KeyValueMapperEngine() {
				// prevents instantiation
			}

			private static final String TYPE_VALUE = "keyvalue";

			private static final String KEY_PREFIX = ALL_PREFIX + "key.";
			public static final String KEY_INIT = KEY_PREFIX + "init";
			public static final String KEY_END = KEY_PREFIX + "end";

			private static final String VALUE_PREFIX = ALL_PREFIX + "value.";
			public static final String VALUE_INIT = VALUE_PREFIX + "init";
			public static final String VALUE_END = VALUE_PREFIX + "end";

		}

	}

	/**
	 * Container for all {@link StartWorkflowTaskDefinition} parameter names.
	 */
	public static class StartWorkflow {

		private StartWorkflow() {
			// prevents instantiation
		}

		public static final String CLASSNAME = "classname";
		public static final String ATTRIBUTES = "attributes";

	}

	/**
	 * Container for all {@link SynchronousEventTaskDefinition} parameter names.
	 */
	public static class SynchronousEvent {

		private SynchronousEvent() {
			// prevents instantiation
		}

		public static final String PHASE = "phase";

		public static final String PHASE_AFTER_CREATE = "after_create";
		public static final String PHASE_BEFORE_UPDATE = "before_update";
		public static final String PHASE_AFTER_UPDATE = "after_update";
		public static final String PHASE_BEFORE_DELETE = "before_delete";

		private static final String FILTER = "filter.";
		public static final String FILTER_GROUPS = FILTER + "groups";
		public static final String FILTER_CLASSNAME = FILTER + "classname";
		public static final String FILTER_CARDS = FILTER + "cards";

		private static final String ACTION_PREFIX = "action.";

		private static final String EMAIL_PREFIX = ACTION_PREFIX + "email.";
		public static final String EMAIL_ACTIVE = EMAIL_PREFIX + "active";
		public static final String EMAIL_ACCOUNT = EMAIL_PREFIX + "account";
		public static final String EMAIL_TEMPLATE = EMAIL_PREFIX + "template";

		private static final String WORKFLOW_PREFIX = ACTION_PREFIX + "workflow.";
		public static final String WORKFLOW_ACTIVE = WORKFLOW_PREFIX + "active";
		public static final String WORKFLOW_CLASS_NAME = WORKFLOW_PREFIX + "classname";
		public static final String WORKFLOW_ATTRIBUTES = WORKFLOW_PREFIX + "attributes";
		public static final String WORKFLOW_ADVANCE = WORKFLOW_PREFIX + "advance";

		private static final String ACTION_SCRIPT_PREFIX = ACTION_PREFIX + "scripting.";
		public static final String ACTION_SCRIPT_ACTIVE = ACTION_SCRIPT_PREFIX + "active";
		public static final String ACTION_SCRIPT_ENGINE = ACTION_SCRIPT_PREFIX + "engine";
		public static final String ACTION_SCRIPT_SCRIPT = ACTION_SCRIPT_PREFIX + "script";
		public static final String ACTION_SCRIPT_SAFE = ACTION_SCRIPT_PREFIX + "safe";

	}

	private static enum SqlSourceHandler {

		MYSQL("mysql", mysql()), //
		ORACLE("oracle", oracle()), //
		POSTGRES("postgresql", postgresql()), //
		SQLSERVER("sqlserver", sqlserver()), //
		UNKNOWN(null, null);
		;

		public static SqlSourceHandler of(final String store) {
			for (final SqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.store, store)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public static SqlSourceHandler of(final DataSourceType type) {
			for (final SqlSourceHandler value : values()) {
				if (ObjectUtils.equals(value.type, type)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		public final String store;
		public final DataSourceType type;

		private SqlSourceHandler(final String client, final DataSourceType server) {
			this.store = client;
			this.type = server;
		}

	}

	private static final Function<ClassMapping, String> CLASS_MAPPING_TO_STRING = new Function<ClassMapping, String>() {

		@Override
		public String apply(final ClassMapping input) {
			return Joiner.on(Connector.MAPPING_SEPARATOR) //
					.join(asList( //
							input.getSourceType(), //
							input.getTargetType(), //
							Boolean.toString(input.isCreate()), //
							Boolean.toString(input.isUpdate()), //
							Boolean.toString(input.isDelete()) //
					));
		}

	};

	private static final Function<AttributeMapping, String> ATTRIBUTE_MAPPING_TO_STRING = new Function<AttributeMapping, String>() {

		@Override
		public String apply(final AttributeMapping input) {
			return Joiner.on(Connector.MAPPING_SEPARATOR) //
					.join(asList( //
							input.getSourceType(), //
							input.getSourceAttribute(), //
							input.getTargetType(), //
							input.getTargetAttribute(), //
							Boolean.toString(input.isKey()) //
					));
		}

	};

	private static final Function<String, ClassMapping> STRING_TO_CLASS_MAPPING = new Function<String, ClassMapping>() {

		@Override
		public ClassMapping apply(final String input) {
			final List<String> elements = Splitter.on(Connector.MAPPING_SEPARATOR).splitToList(input);
			return ClassMapping.newInstance() //
					.withSourceType(elements.get(0)) //
					.withTargetType(elements.get(1)) //
					.withCreateStatus(Boolean.parseBoolean(elements.get(2))) //
					.withUpdateStatus(Boolean.parseBoolean(elements.get(3))) //
					.withDeleteStatus(Boolean.parseBoolean(elements.get(4))) //
					.build();
		}

	};

	private static final Function<String, AttributeMapping> STRING_TO_ATTRIBUTE_MAPPING = new Function<String, AttributeMapping>() {

		@Override
		public AttributeMapping apply(final String input) {
			final List<String> elements = Splitter.on(Connector.MAPPING_SEPARATOR).splitToList(input);
			return AttributeMapping.newInstance() //
					.withSourceType(elements.get(0)) //
					.withSourceAttribute(elements.get(1)) //
					.withTargetType(elements.get(2)) //
					.withTargetAttribute(elements.get(3)) //
					.withKeyStatus(Boolean.parseBoolean(elements.get(4))) //
					.build();
		}

	};

	private static final String KEY_VALUE_SEPARATOR = "=";
	private static final String GROUPS_SEPARATOR = ",";

	private static final Logger logger = TaskManagerLogic.logger;
	private static final Marker marker = MarkerFactory.getMarker(DefaultLogicAndStoreConverter.class.getName());

	private static class MapperToParametersConverter implements MapperEngineVisitor {

		public static MapperToParametersConverter of(final MapperEngine mapper) {
			return new MapperToParametersConverter(mapper);
		}

		private final MapperEngine mapper;

		private MapperToParametersConverter(final MapperEngine mapper) {
			this.mapper = mapper;
		}

		private Map<String, String> parameters;

		public Map<String, String> convert() {
			parameters = Maps.newLinkedHashMap();
			mapper.accept(this);
			return parameters;
		}

		@Override
		public void visit(final KeyValueMapperEngine mapper) {
			parameters.put(ReadEmail.MapperEngine.TYPE, ReadEmail.KeyValueMapperEngine.TYPE_VALUE);
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_INIT, mapper.getKeyInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.KEY_END, mapper.getKeyEnd());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_INIT, mapper.getValueInit());
			parameters.put(ReadEmail.KeyValueMapperEngine.VALUE_END, mapper.getValueEnd());
		}

		@Override
		public void visit(final NullMapperEngine mapper) {
			// nothing to do
		}

	}

	// TODO do in some way with visitor
	private static enum ParametersToMapperConverter {

		KEY_VALUE(ReadEmail.KeyValueMapperEngine.TYPE_VALUE) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return KeyValueMapperEngine.newInstance() //
						.withKey( //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.KEY_END) //
						) //
						.withValue( //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_INIT), //
								parameters.get(ReadEmail.KeyValueMapperEngine.VALUE_END) //
						) //
						.build();
			}

		}, //
		UNDEFINED(EMPTY) {

			@Override
			public MapperEngine convert(final Map<String, String> parameters) {
				return NullMapperEngine.getInstance();
			}

		}, //
		;

		public static ParametersToMapperConverter of(final String type) {
			for (final ParametersToMapperConverter element : values()) {
				if (element.type.equals(type)) {
					return element;
				}
			}
			return UNDEFINED;
		}

		private final String type;

		private ParametersToMapperConverter(final String type) {
			this.type = type;
		}

		public abstract MapperEngine convert(Map<String, String> parameters);

	}

	private static class PhaseToStoreConverter implements SynchronousEventTask.PhaseIdentifier {

		private final SynchronousEventTask task;
		private String converted;

		public PhaseToStoreConverter(final SynchronousEventTask task) {
			this.task = task;
		}

		public String toStore() {
			if (task.getPhase() != null) {
				task.getPhase().identify(this);
				Validate.notNull(converted, "conversion error");
			} else {
				converted = null;
			}
			return converted;
		}

		@Override
		public void afterCreate() {
			converted = SynchronousEvent.PHASE_AFTER_CREATE;
		}

		@Override
		public void beforeUpdate() {
			converted = SynchronousEvent.PHASE_BEFORE_UPDATE;
		}

		@Override
		public void afterUpdate() {
			converted = SynchronousEvent.PHASE_AFTER_UPDATE;
		}

		@Override
		public void beforeDelete() {
			converted = SynchronousEvent.PHASE_BEFORE_DELETE;
		}

	}

	private static class PhaseToLogicConverter {

		private final String stored;

		public PhaseToLogicConverter(final String stored) {
			this.stored = stored;
		}

		public Phase toLogic() {
			final Phase converted;
			if (SynchronousEvent.PHASE_AFTER_CREATE.equals(stored)) {
				converted = Phase.AFTER_CREATE;
			} else if (SynchronousEvent.PHASE_BEFORE_UPDATE.equals(stored)) {
				converted = Phase.BEFORE_UPDATE;
			} else if (SynchronousEvent.PHASE_AFTER_UPDATE.equals(stored)) {
				converted = Phase.AFTER_UPDATE;
			} else if (SynchronousEvent.PHASE_BEFORE_DELETE.equals(stored)) {
				converted = Phase.BEFORE_DELETE;
			} else {
				converted = null;
			}
			return converted;
		}

	}

	private static class DefaultLogicAsSourceConverter implements LogicAsSourceConverter, TaskVistor {

		private final Task source;

		private org.cmdbuild.data.store.task.Task target;

		public DefaultLogicAsSourceConverter(final Task source) {
			this.source = source;
		}

		@Override
		public org.cmdbuild.data.store.task.Task toStore() {
			logger.info(marker, "converting logic task '{}' to store task", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final ConnectorTask task) {
			final SourceConfiguration sourceConfiguration = task.getSourceConfiguration();
			this.target = org.cmdbuild.data.store.task.ConnectorTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(Connector.NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(Connector.NOTIFICATION_ACCOUNT, task.getNotificationAccount()) //
					.withParameter(Connector.NOTIFICATION_ERROR_TEMPLATE, task.getNotificationErrorTemplate()) //
					.withParameters(parametersOf(sourceConfiguration)) //
					.withParameter(Connector.MAPPING_TYPES, Joiner.on(LINE_SEPARATOR) //
							.join( //
							FluentIterable.from(task.getClassMappings()) //
									.transform(CLASS_MAPPING_TO_STRING)) //
					) //
					.withParameter(Connector.MAPPING_ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
							.join( //
							FluentIterable.from(task.getAttributeMappings()) //
									.transform(ATTRIBUTE_MAPPING_TO_STRING)) //
					) //
					.build();
		}

		private Map<String, String> parametersOf(final SourceConfiguration sourceConfiguration) {
			final Map<String, String> parameters = Maps.newHashMap();
			sourceConfiguration.accept(new SourceConfigurationVisitor() {

				@Override
				public void visit(final SqlSourceConfiguration sourceConfiguration) {
					final Map<String, String> map = Maps.newHashMap();
					map.put(Connector.SQL_TYPE, SqlSourceHandler.of(sourceConfiguration.getType()).store);
					map.put(Connector.SQL_HOSTNAME, sourceConfiguration.getHost());
					map.put(Connector.SQL_PORT, Integer.toString(sourceConfiguration.getPort()));
					map.put(Connector.SQL_DATABASE, sourceConfiguration.getDatabase());
					map.put(Connector.SQL_INSTANCE, sourceConfiguration.getInstance());
					map.put(Connector.SQL_USERNAME, sourceConfiguration.getUsername());
					map.put(Connector.SQL_PASSWORD, sourceConfiguration.getPassword());
					map.put(Connector.SQL_FILTER, sourceConfiguration.getFilter());
					parameters.put(Connector.DATA_SOURCE_TYPE, "sql");
					parameters.put(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.useForNull(EMPTY) //
							.join(map));
				}
			});
			return parameters;
		}

		@Override
		public void visit(final ReadEmailTask task) {
			this.target = org.cmdbuild.data.store.task.ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(ReadEmail.ACCOUNT_NAME, task.getEmailAccount()) //
					.withParameter(ReadEmail.FILTER_FROM_REGEX, Joiner.on(LINE_SEPARATOR) //
							.join(task.getRegexFromFilter())) //
					.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, Joiner.on(LINE_SEPARATOR) //
							.join(task.getRegexSubjectFilter())) //
					.withParameter(ReadEmail.NOTIFICATION_ACTIVE, //
							Boolean.toString(task.isNotificationActive())) //
					.withParameter(ReadEmail.NOTIFICATION_TEMPLATE, //
							defaultString(task.getNotificationTemplate())) //
					.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, //
							Boolean.toString(task.isAttachmentsActive())) //
					.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, //
							task.getAttachmentsCategory()) //
					.withParameter(ReadEmail.WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowActive())) //
					.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getWorkflowAttributes())) //
					.withParameter(ReadEmail.WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, //
							Boolean.toString(task.isWorkflowAttachments())) //
					.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, //
							task.getWorkflowAttachmentsCategory()) //
					.withParameters(MapperToParametersConverter.of(task.getMapperEngine()).convert()) //
					.build();
		}

		@Override
		public void visit(final StartWorkflowTask task) {
			this.target = org.cmdbuild.data.store.task.StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withCronExpression(task.getCronExpression()) //
					.withParameter(StartWorkflow.CLASSNAME, task.getProcessClass()) //
					.withParameter(StartWorkflow.ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getAttributes())) //
					.build();
		}

		@Override
		public void visit(final SynchronousEventTask task) {
			this.target = org.cmdbuild.data.store.task.SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withRunningStatus(task.isActive()) //
					.withParameter(SynchronousEvent.PHASE, new PhaseToStoreConverter(task).toStore()) //
					.withParameter(SynchronousEvent.FILTER_GROUPS, Joiner.on(GROUPS_SEPARATOR) //
							.join(task.getGroups())) //
					.withParameter(SynchronousEvent.FILTER_CLASSNAME, task.getTargetClassname()) //
					.withParameter(SynchronousEvent.FILTER_CARDS, task.getFilter()) //
					.withParameter(SynchronousEvent.EMAIL_ACTIVE, //
							Boolean.toString(task.isEmailEnabled())) //
					.withParameter(SynchronousEvent.EMAIL_ACCOUNT, task.getEmailAccount()) //
					.withParameter(SynchronousEvent.EMAIL_TEMPLATE, task.getEmailTemplate()) //
					.withParameter(SynchronousEvent.WORKFLOW_ACTIVE, //
							Boolean.toString(task.isWorkflowEnabled())) //
					.withParameter(SynchronousEvent.WORKFLOW_CLASS_NAME, task.getWorkflowClassName()) //
					.withParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
							.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
							.join(task.getWorkflowAttributes())) //
					.withParameter(SynchronousEvent.WORKFLOW_ADVANCE, //
							Boolean.toString(task.isWorkflowAdvanceable())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, Boolean.toString(task.isScriptingEnabled())) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, task.getScriptingEngine()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, task.getScriptingScript()) //
					.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, Boolean.toString(task.isScriptingSafe())) //
					.build();
		}
	}

	private static class DefaultStoreAsSourceConverter implements StoreAsSourceConverter, TaskVisitor {

		private static final Iterable<ClassMapping> NO_CLASS_MAPPINGS = Collections.emptyList();
		private static final Iterable<AttributeMapping> NO_ATTRIBUTE_MAPPINGS = Collections.emptyList();

		private static final Iterable<String> EMPTY_GROUPS = Collections.emptyList();
		private static final Iterable<String> EMPTY_FILTERS = Collections.emptyList();
		private static final Map<String, String> EMPTY_PARAMETERS = Collections.emptyMap();

		private final org.cmdbuild.data.store.task.Task source;

		private Task target;

		public DefaultStoreAsSourceConverter(final org.cmdbuild.data.store.task.Task source) {
			this.source = source;
		}

		@Override
		public Task toLogic() {
			logger.info(marker, "converting store task '{}' to logic task", source);
			source.accept(this);
			Validate.notNull(target, "conversion error");
			return target;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ConnectorTask task) {
			final String dataSourceType = task.getParameter(Connector.DATA_SOURCE_TYPE);
			final String dataSourceConfiguration = task.getParameter(Connector.DATA_SOURCE_CONFIGURATION);
			final String typeMapping = task.getParameter(Connector.MAPPING_TYPES);
			final String attributeMapping = task.getParameter(Connector.MAPPING_ATTRIBUTES);
			target = ConnectorTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(Connector.NOTIFICATION_ACTIVE))) //
					.withNotificationAccount(task.getParameter(Connector.NOTIFICATION_ACCOUNT)) //
					.withNotificationErrorTemplate(task.getParameter(Connector.NOTIFICATION_ERROR_TEMPLATE)) //
					.withSourceConfiguration(sourceConfigurationOf(dataSourceType, dataSourceConfiguration)) //
					.withClassMappings( //
							isEmpty(typeMapping) ? NO_CLASS_MAPPINGS : FluentIterable.from( //
									Splitter.on(LINE_SEPARATOR) //
											.split(typeMapping)) //
									.transform(STRING_TO_CLASS_MAPPING)) //
					.withAttributeMappings( //
							isEmpty(attributeMapping) ? NO_ATTRIBUTE_MAPPINGS : FluentIterable.from( //
									Splitter.on(LINE_SEPARATOR) //
											.split(attributeMapping)) //
									.transform(STRING_TO_ATTRIBUTE_MAPPING)) //
					.build();
		}

		private SourceConfiguration sourceConfigurationOf(final String type, final String configuration) {
			final SourceConfiguration sourceConfiguration;
			if (isBlank(configuration)) {
				sourceConfiguration = NULL_SOURCE_CONFIGURATION;
			} else {
				final Map<String, String> map = Splitter.on(LINE_SEPARATOR) //
						.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
						.split(defaultString(configuration));
				sourceConfiguration = SqlSourceConfiguration.newInstance() //
						.withType(SqlSourceHandler.of(map.get(Connector.SQL_TYPE)).type) //
						.withHost(map.get(Connector.SQL_HOSTNAME)) //
						.withPort(createInteger(defaultString(map.get(Connector.SQL_PORT), null))) //
						.withDatabase(map.get(Connector.SQL_DATABASE)) //
						.withInstance(map.get(Connector.SQL_INSTANCE)) //
						.withUsername(map.get(Connector.SQL_USERNAME)) //
						.withPassword(map.get(Connector.SQL_PASSWORD)) //
						.withFilter(map.get(Connector.SQL_FILTER)) //
						.build();
			}
			return sourceConfiguration;
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.ReadEmailTask task) {
			final String fromRegexFilters = task.getParameter(ReadEmail.FILTER_FROM_REGEX);
			final String subjectRegexFilters = task.getParameter(ReadEmail.FILTER_SUBJECT_REGEX);
			final String attributesAsString = defaultString(task.getParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING));
			target = ReadEmailTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withEmailAccount(task.getParameter(ReadEmail.ACCOUNT_NAME)) //
					.withRegexFromFilter( //
							isEmpty(fromRegexFilters) ? EMPTY_FILTERS : Splitter.on(LINE_SEPARATOR) //
									.split(fromRegexFilters)) //
					.withRegexSubjectFilter( //
							isEmpty(subjectRegexFilters) ? EMPTY_FILTERS : Splitter.on(LINE_SEPARATOR) //
									.split(subjectRegexFilters)) //
					.withNotificationStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.NOTIFICATION_ACTIVE))) //
					.withNotificationTemplate( //
							task.getParameter(ReadEmail.NOTIFICATION_TEMPLATE)) //
					.withAttachmentsActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.ATTACHMENTS_ACTIVE))) //
					.withAttachmentsCategory(task.getParameter(ReadEmail.ATTACHMENTS_CATEGORY)) //
					.withWorkflowActive( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(ReadEmail.WORKFLOW_CLASS_NAME)) //
					.withWorkflowAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.split(attributesAsString)) //
					.withWorkflowAdvanceableStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ADVANCE))) //
					.withWorkflowAttachmentsStatus( //
							Boolean.valueOf(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE))) //
					.withWorkflowAttachmentsCategory(task.getParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY)) //
					.withMapperEngine(mapperOf(task.getParameters())) //
					.build();
		}

		private MapperEngine mapperOf(final Map<String, String> parameters) {
			final String type = parameters.get(ReadEmail.MapperEngine.TYPE);
			return ParametersToMapperConverter.of(type).convert(parameters);
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.StartWorkflowTask task) {
			final String attributesAsString = defaultString(task.getParameter(StartWorkflow.ATTRIBUTES));
			target = StartWorkflowTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withCronExpression(task.getCronExpression()) //
					.withProcessClass(task.getParameter(StartWorkflow.CLASSNAME)) //
					.withAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.split(attributesAsString)) //
					.build();
		}

		@Override
		public void visit(final org.cmdbuild.data.store.task.SynchronousEventTask task) {
			final String groupsAsString = defaultString(task.getParameter(SynchronousEvent.FILTER_GROUPS));
			final String attributesAsString = defaultString(task.getParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES));
			target = SynchronousEventTask.newInstance() //
					.withId(task.getId()) //
					.withDescription(task.getDescription()) //
					.withActiveStatus(task.isRunning()) //
					.withPhase( //
							new PhaseToLogicConverter(task.getParameter(SynchronousEvent.PHASE)) //
									.toLogic()) //
					.withGroups(isEmpty(groupsAsString) ? EMPTY_GROUPS : Splitter.on(GROUPS_SEPARATOR) //
							.split(groupsAsString)) //
					.withTargetClass(task.getParameter(SynchronousEvent.FILTER_CLASSNAME)) //
					.withFilter(task.getParameter(SynchronousEvent.FILTER_CARDS)) //
					.withEmailEnabled( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.EMAIL_ACTIVE))) //
					.withEmailAccount(task.getParameter(SynchronousEvent.EMAIL_ACCOUNT)) //
					.withEmailTemplate(task.getParameter(SynchronousEvent.EMAIL_TEMPLATE)) //
					.withWorkflowEnabled( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.WORKFLOW_ACTIVE))) //
					.withWorkflowClassName(task.getParameter(SynchronousEvent.WORKFLOW_CLASS_NAME)) //
					.withWorkflowAttributes( //
							isEmpty(attributesAsString) ? EMPTY_PARAMETERS : Splitter.on(LINE_SEPARATOR) //
									.withKeyValueSeparator(KEY_VALUE_SEPARATOR) //
									.split(attributesAsString)) //
					.withWorkflowAdvanceable( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.WORKFLOW_ADVANCE))) //
					.withScriptingEnableStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE))) //
					.withScriptingEngine(task.getParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE)) //
					.withScript(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT)) //
					.withScriptingSafeStatus( //
							Boolean.valueOf(task.getParameter(SynchronousEvent.ACTION_SCRIPT_SAFE))) //
					.build();
		}
	}

	@Override
	public LogicAsSourceConverter from(final Task source) {
		return new DefaultLogicAsSourceConverter(source);
	}

	@Override
	public StoreAsSourceConverter from(final org.cmdbuild.data.store.task.Task source) {
		return new DefaultStoreAsSourceConverter(source);
	}

}
