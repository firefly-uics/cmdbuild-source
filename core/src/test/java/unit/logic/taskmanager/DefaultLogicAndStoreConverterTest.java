package unit.logic.taskmanager;

import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.SystemUtils.LINE_SEPARATOR;
import static org.cmdbuild.common.utils.BuilderUtils.a;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.MySqlSourceType.mysql;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.OracleSourceType.oracle;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.PostgreSqlSourceType.postgresql;
import static org.cmdbuild.logic.taskmanager.ConnectorTask.SqlServerSourceType.sqlserver;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.cmdbuild.logic.taskmanager.ConnectorTask;
import org.cmdbuild.logic.taskmanager.ConnectorTask.AttributeMapping;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SourceConfiguration;
import org.cmdbuild.logic.taskmanager.ConnectorTask.SqlSourceConfiguration;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.Connector;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.ReadEmail;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.StartWorkflow;
import org.cmdbuild.logic.taskmanager.DefaultLogicAndStoreConverter.SynchronousEvent;
import org.cmdbuild.logic.taskmanager.KeyValueMapperEngine;
import org.cmdbuild.logic.taskmanager.MapperEngine;
import org.cmdbuild.logic.taskmanager.ReadEmailTask;
import org.cmdbuild.logic.taskmanager.StartWorkflowTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask;
import org.cmdbuild.logic.taskmanager.SynchronousEventTask.Phase;
import org.cmdbuild.logic.taskmanager.Task;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

public class DefaultLogicAndStoreConverterTest {

	private static Comparator<AttributeMapping> ATTRIBUTE_MAPPING_COMPARATOR = new Comparator<AttributeMapping>() {

		@Override
		public int compare(final AttributeMapping o1, final AttributeMapping o2) {
			return o1.getSourceType().compareTo(o2.getSourceType());
		}

	};

	private DefaultLogicAndStoreConverter converter;

	@Before
	public void setUp() throws Exception {
		converter = new DefaultLogicAndStoreConverter();
	}

	@Test
	public void connectorTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask source = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withAttributeMapping(a(AttributeMapping.newInstance() //
						.withSourceType("sourceTypeA") //
						.withSourceAttribute("sourceAttributeA") //
						.withTargetType("targetTypeA") //
						.withTargetAttribute("targetAttributeA") //
						.withKeyStatus(true) //
						)) //
				.withAttributeMapping(a(AttributeMapping.newInstance() //
						.withSourceType("sourceTypeB") //
						.withSourceAttribute("sourceAttributeB") //
						.withTargetType("targetTypeB") //
						.withTargetAttribute("targetAttributeB") //
						.withKeyStatus(false) //
						)) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(Connector.MAPPING_TYPE, "" //
				+ "sourceTypeA,sourceAttributeA,targetTypeA,targetAttributeA,true" + LINE_SEPARATOR //
				+ "sourceTypeB,sourceAttributeB,targetTypeB,targetAttributeB,false" //
		));
	}

	@Test
	public void sqlDataSourceSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask source = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(postgresql()) //
						.withHost("example.com") //
						.withPort(12345) //
						.withDatabase("db") //
						.withUsername("user") //
						.withPassword("pwd") //
						.withFilter("filter") //
						)) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(Connector.DATA_SOURCE_TYPE, "sql"));

		final Map<String, String> configuration = Splitter.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.split(parameters.get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "postgresql"));
		assertThat(configuration, hasEntry(Connector.SQL_HOSTNAME, "example.com"));
		assertThat(configuration, hasEntry(Connector.SQL_PORT, "12345"));
		assertThat(configuration, hasEntry(Connector.SQL_DATABASE, "db"));
		assertThat(configuration, hasEntry(Connector.SQL_USERNAME, "user"));
		assertThat(configuration, hasEntry(Connector.SQL_PASSWORD, "pwd"));
		assertThat(configuration, hasEntry(Connector.SQL_FILTER, "filter"));
	}

	@Test
	public void mysqlTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(mysql()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "mysql"));
	}

	@Test
	public void oracleTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(oracle()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "oracle"));
	}

	@Test
	public void postgresqlTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(postgresql()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "postgresql"));
	}

	@Test
	public void sqlserverTypeForSqlDataSuccessfullyConvertedToStore() throws Exception {
		// given
		final ConnectorTask sourceWithMySql = a(ConnectorTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withSourceConfiguration(a(SqlSourceConfiguration.newInstance() //
						.withType(sqlserver()))) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(sourceWithMySql).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.ConnectorTask.class));

		final Map<String, String> configuration = Splitter.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.split(converted.getParameters().get(Connector.DATA_SOURCE_CONFIGURATION));
		assertThat(configuration, hasEntry(Connector.SQL_TYPE, "sqlserver"));
	}

	@Test
	public void connectorTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.MAPPING_TYPE, "" //
						+ "sourceTypeA,sourceAttributeA,targetTypeA,targetAttributeA,true" + LINE_SEPARATOR //
						+ "sourceTypeB,sourceAttributeB,targetTypeB,targetAttributeB,false" //
				) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getCronExpression(), equalTo("cron expression"));
		final List<AttributeMapping> attributeMappings = Ordering.from(ATTRIBUTE_MAPPING_COMPARATOR) //
				.immutableSortedCopy(converted.getAttributeMappings());
		final AttributeMapping first = attributeMappings.get(0);
		assertThat(first.getSourceType(), equalTo("sourceTypeA"));
		assertThat(first.getSourceAttribute(), equalTo("sourceAttributeA"));
		assertThat(first.getTargetType(), equalTo("targetTypeA"));
		assertThat(first.getTargetAttribute(), equalTo("targetAttributeA"));
		assertThat(first.isKey(), is(true));
		final AttributeMapping second = attributeMappings.get(1);
		assertThat(second.getSourceType(), equalTo("sourceTypeB"));
		assertThat(second.getSourceAttribute(), equalTo("sourceAttributeB"));
		assertThat(second.getTargetType(), equalTo("targetTypeB"));
		assertThat(second.getTargetAttribute(), equalTo("targetAttributeB"));
		assertThat(second.isKey(), is(false));
	}

	@Test
	public void sqlDataSourceSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = Maps.newHashMap();
		configuration.put(Connector.SQL_TYPE, "postgresql");
		configuration.put(Connector.SQL_HOSTNAME, "example.com");
		configuration.put(Connector.SQL_PORT, "12345");
		configuration.put(Connector.SQL_DATABASE, "db");
		configuration.put(Connector.SQL_USERNAME, "user");
		configuration.put(Connector.SQL_PASSWORD, "pwd");
		configuration.put(Connector.SQL_FILTER, "filter");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
				.withParameter(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
						.withKeyValueSeparator("=") //
						.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(postgresql()) //
				.withHost("example.com") //
				.withPort(12345) //
				.withDatabase("db") //
				.withUsername("user") //
				.withPassword("pwd") //
				.withFilter("filter") //
				)) //
		);
	}

	@Test
	public void mysqlTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = Maps.newHashMap();
		configuration.put(Connector.SQL_TYPE, "mysql");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
				.withParameter(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
						.withKeyValueSeparator("=") //
						.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(mysql()))) //
		);
	}

	@Test
	public void oracleTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = Maps.newHashMap();
		configuration.put(Connector.SQL_TYPE, "oracle");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
				.withParameter(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
						.withKeyValueSeparator("=") //
						.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(oracle()))) //
		);
	}

	@Test
	public void postgresqlTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = Maps.newHashMap();
		configuration.put(Connector.SQL_TYPE, "postgresql");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
				.withParameter(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
						.withKeyValueSeparator("=") //
						.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(postgresql()))) //
		);
	}

	@Test
	public void sqlserverTypeForSqlDataSuccessfullyConvertedToLogic() throws Exception {
		// given
		final Map<String, String> configuration = Maps.newHashMap();
		configuration.put(Connector.SQL_TYPE, "sqlserver");
		final org.cmdbuild.data.store.task.ConnectorTask source = a(org.cmdbuild.data.store.task.ConnectorTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(Connector.DATA_SOURCE_TYPE, "sql") //
				.withParameter(Connector.DATA_SOURCE_CONFIGURATION, Joiner.on(LINE_SEPARATOR) //
						.withKeyValueSeparator("=") //
						.join(configuration)) //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ConnectorTask.class));
		final ConnectorTask converted = ConnectorTask.class.cast(_converted);
		final SourceConfiguration sourceConfiguration = converted.getSourceConfiguration();
		assertThat(sourceConfiguration, instanceOf(SqlSourceConfiguration.class));
		final SqlSourceConfiguration sqlSourceConfiguration = SqlSourceConfiguration.class.cast(sourceConfiguration);
		assertThat(sqlSourceConfiguration, equalTo(a(SqlSourceConfiguration.newInstance() //
				.withType(sqlserver()))) //
		);
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final ReadEmailTask source = a(ReadEmailTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withEmailAccount("email account") //
				.withRegexFromFilter(asList("regex", "from", "filter")) //
				.withRegexSubjectFilter(asList("regex", "subject", "filter")) //
				.withNotificationStatus(true) //
				.withNotificationTemplate("template") //
				.withAttachmentsActive(true) //
				.withAttachmentsCategory("category") //
				.withWorkflowActive(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowAttributes(attributes) //
				.withWorkflowAdvanceableStatus(true) //
				.withWorkflowAttachmentsStatus(true) //
				.withWorkflowAttachmentsCategory("workflow's attachments category") //
		);

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
		assertThat(parameters, hasEntry(ReadEmail.FILTER_FROM_REGEX, "regex\nfrom\nfilter"));
		assertThat(parameters, hasEntry(ReadEmail.FILTER_SUBJECT_REGEX, "regex\nsubject\nfilter"));
		assertThat(parameters, hasEntry(ReadEmail.NOTIFICATION_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.NOTIFICATION_TEMPLATE, "template"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.ATTACHMENTS_CATEGORY, "category"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_FIELDS_MAPPING, Joiner.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.join(attributes)));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ADVANCE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true"));
		assertThat(parameters, hasEntry(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToStore() throws Exception {
		// given
		final ReadEmailTask source = a(ReadEmailTask.newInstance() //
				.withMapperEngine(a(KeyValueMapperEngine.newInstance() //
						.withKey("key_init", "key_end") //
						.withValue("value_init", "value_end") //
						) //
				) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init"));
		assertThat(parameters, hasEntry(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end"));
	}

	@Test
	public void readEmailTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = a(org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(ReadEmail.ACCOUNT_NAME, "email account") //
				.withParameter(ReadEmail.FILTER_FROM_REGEX, "regex\nfrom\nfilter") //
				.withParameter(ReadEmail.FILTER_SUBJECT_REGEX, "regex\nsubject\nfilter") //
				.withParameter(ReadEmail.NOTIFICATION_ACTIVE, "true") //
				.withParameter(ReadEmail.NOTIFICATION_TEMPLATE, "template") //
				.withParameter(ReadEmail.ATTACHMENTS_ACTIVE, "true") //
				.withParameter(ReadEmail.ATTACHMENTS_CATEGORY, "category") //
				.withParameter(ReadEmail.WORKFLOW_ACTIVE, "true") //
				.withParameter(ReadEmail.WORKFLOW_CLASS_NAME, "workflow class name") //
				.withParameter(ReadEmail.WORKFLOW_FIELDS_MAPPING, "foo=bar\nbar=baz\nbaz=foo") //
				.withParameter(ReadEmail.WORKFLOW_ADVANCE, "true") //
				.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_SAVE, "true") //
				.withParameter(ReadEmail.WORKFLOW_ATTACHMENTS_CATEGORY, "workflow's attachments category") //
		);

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
		assertThat(converted.isNotificationActive(), equalTo(true));
		assertThat(converted.getNotificationTemplate(), equalTo("template"));
		assertThat(converted.getRegexFromFilter(), containsInAnyOrder("regex", "from", "filter"));
		assertThat(converted.getRegexSubjectFilter(), containsInAnyOrder("regex", "subject", "filter"));
		assertThat(converted.isAttachmentsActive(), equalTo(true));
		assertThat(converted.getAttachmentsCategory(), equalTo("category"));
		assertThat(converted.isWorkflowActive(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		assertThat(converted.getWorkflowAttributes(), equalTo(attributes));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isWorkflowAttachments(), equalTo(true));
		assertThat(converted.getWorkflowAttachmentsCategory(), equalTo("workflow's attachments category"));
	}

	@Test
	public void keyValueMapperSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.ReadEmailTask source = a(org.cmdbuild.data.store.task.ReadEmailTask
				.newInstance() //
				.withParameter(ReadEmail.KeyValueMapperEngine.TYPE, "keyvalue") //
				.withParameter(ReadEmail.KeyValueMapperEngine.KEY_INIT, "key_init") //
				.withParameter(ReadEmail.KeyValueMapperEngine.KEY_END, "key_end") //
				.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_INIT, "value_init") //
				.withParameter(ReadEmail.KeyValueMapperEngine.VALUE_END, "value_end") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(ReadEmailTask.class));
		final ReadEmailTask converted = ReadEmailTask.class.cast(_converted);
		final MapperEngine mapper = converted.getMapperEngine();
		assertThat(mapper, instanceOf(KeyValueMapperEngine.class));
		final KeyValueMapperEngine keyValueMapper = KeyValueMapperEngine.class.cast(mapper);
		assertThat(keyValueMapper.getKeyInit(), equalTo("key_init"));
		assertThat(keyValueMapper.getKeyEnd(), equalTo("key_end"));
		assertThat(keyValueMapper.getValueInit(), equalTo("value_init"));
		assertThat(keyValueMapper.getValueEnd(), equalTo("value_end"));
	}

	@Test
	public void startWorkflowTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final StartWorkflowTask source = a(StartWorkflowTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withCronExpression("cron expression") //
				.withProcessClass("class name") //
				.withAttributes(attributes) //
		);

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
		final org.cmdbuild.data.store.task.StartWorkflowTask source = a(org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, "foo=bar\nbar=baz\nbaz=foo") //
		);

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
		final org.cmdbuild.data.store.task.StartWorkflowTask source = a(org.cmdbuild.data.store.task.StartWorkflowTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withCronExpression("cron expression") //
				.withParameter(StartWorkflow.CLASSNAME, "class name") //
				.withParameter(StartWorkflow.ATTRIBUTES, EMPTY) //
		);

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
		final Map<String, String> attributes = Maps.newHashMap();
		attributes.put("foo", "bar");
		attributes.put("bar", "baz");
		attributes.put("baz", "foo");
		final SynchronousEventTask source = a(SynchronousEventTask.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withActiveStatus(true) //
				.withPhase(Phase.AFTER_CREATE) //
				.withGroups(asList("foo", "bar", "baz")) //
				.withTargetClass("classname") //
				.withFilter("card's filter") //
				.withEmailEnabled(true) //
				.withEmailAccount("email account") //
				.withEmailTemplate("email template") //
				.withWorkflowEnabled(true) //
				.withWorkflowClassName("workflow class name") //
				.withWorkflowAttributes(attributes) //
				.withScriptingEnableStatus(true) //
				.withScriptingEngine("groovy") //
				.withScript("blah blah blah") //
				.withScriptingSafeStatus(true) //
		);

		// when
		final org.cmdbuild.data.store.task.Task converted = converter.from(source).toStore();

		// then
		assertThat(converted, instanceOf(org.cmdbuild.data.store.task.SynchronousEventTask.class));
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isRunning(), equalTo(true));

		final Map<String, String> parameters = converted.getParameters();
		assertThat(parameters, hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_CLASSNAME, "classname"));
		assertThat(parameters, hasEntry(SynchronousEvent.FILTER_CARDS, "card's filter"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_ACCOUNT, "email account"));
		assertThat(parameters, hasEntry(SynchronousEvent.EMAIL_TEMPLATE, "email template"));
		assertThat(parameters, hasEntry(SynchronousEvent.WORKFLOW_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.WORKFLOW_CLASS_NAME, "workflow class name"));
		assertThat(parameters, hasEntry(SynchronousEvent.WORKFLOW_ATTRIBUTES, Joiner.on(LINE_SEPARATOR) //
				.withKeyValueSeparator("=") //
				.join(attributes)));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah"));
		assertThat(parameters, hasEntry(SynchronousEvent.ACTION_SCRIPT_SAFE, "true"));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToStore() throws Exception {
		// given
		final SynchronousEventTask afterCreate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_CREATE) //
		);
		final SynchronousEventTask beforeUpdate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_UPDATE) //
		);
		final SynchronousEventTask afterUpdate = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.AFTER_UPDATE) //
		);
		final SynchronousEventTask beforeDelete = a(SynchronousEventTask.newInstance() //
				.withPhase(Phase.BEFORE_DELETE) //
		);

		// when
		final org.cmdbuild.data.store.task.Task convertedAfterCreate = converter.from(afterCreate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeUpdate = converter.from(beforeUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedAfterUpdate = converter.from(afterUpdate).toStore();
		final org.cmdbuild.data.store.task.Task convertedBeforeDelete = converter.from(beforeDelete).toStore();

		// then
		assertThat(convertedAfterCreate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_create"));
		assertThat(convertedBeforeUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_update"));
		assertThat(convertedAfterUpdate.getParameters(), hasEntry(SynchronousEvent.PHASE, "after_update"));
		assertThat(convertedBeforeDelete.getParameters(), hasEntry(SynchronousEvent.PHASE, "before_delete"));
	}

	@Test
	public void synchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask source = a(org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withId(42L) //
				.withDescription("description") //
				.withRunningStatus(true) //
				.withParameter(SynchronousEvent.PHASE, "after_create") //
				.withParameter(SynchronousEvent.FILTER_GROUPS, "foo,bar,baz") //
				.withParameter(SynchronousEvent.FILTER_CLASSNAME, "classname") //
				.withParameter(SynchronousEvent.FILTER_CARDS, "card's filter") //
				.withParameter(SynchronousEvent.EMAIL_ACTIVE, "true") //
				.withParameter(SynchronousEvent.EMAIL_ACCOUNT, "email account") //
				.withParameter(SynchronousEvent.EMAIL_TEMPLATE, "email template") //
				.withParameter(SynchronousEvent.WORKFLOW_ACTIVE, "true") //
				.withParameter(SynchronousEvent.WORKFLOW_CLASS_NAME, "workflow class name") //
				.withParameter(SynchronousEvent.WORKFLOW_ATTRIBUTES, "foo=bar\nbar=baz\nbaz=foo") //
				.withParameter(SynchronousEvent.WORKFLOW_ADVANCE, "true") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ACTIVE, "true") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_ENGINE, "groovy") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_SCRIPT, "blah blah blah") //
				.withParameter(SynchronousEvent.ACTION_SCRIPT_SAFE, "true") //
		);

		// when
		final Task _converted = converter.from(source).toLogic();

		// then
		assertThat(_converted, instanceOf(SynchronousEventTask.class));
		final SynchronousEventTask converted = SynchronousEventTask.class.cast(_converted);
		assertThat(converted.getId(), equalTo(42L));
		assertThat(converted.getDescription(), equalTo("description"));
		assertThat(converted.isActive(), equalTo(true));
		assertThat(converted.getPhase(), equalTo(Phase.AFTER_CREATE));
		assertThat(converted.getGroups(), containsInAnyOrder("foo", "bar", "baz"));
		assertThat(converted.getTargetClassname(), equalTo("classname"));
		assertThat(converted.getFilter(), equalTo("card's filter"));
		assertThat(converted.isEmailEnabled(), equalTo(true));
		assertThat(converted.getEmailAccount(), equalTo("email account"));
		assertThat(converted.getEmailTemplate(), equalTo("email template"));
		assertThat(converted.isWorkflowEnabled(), equalTo(true));
		assertThat(converted.getWorkflowClassName(), equalTo("workflow class name"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("foo", "bar"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("bar", "baz"));
		assertThat(converted.getWorkflowAttributes(), hasEntry("baz", "foo"));
		assertThat(converted.isWorkflowAdvanceable(), equalTo(true));
		assertThat(converted.isScriptingEnabled(), equalTo(true));
		assertThat(converted.getScriptingEngine(), equalTo("groovy"));
		assertThat(converted.getScriptingScript(), equalTo("blah blah blah"));
		assertThat(converted.isScriptingSafe(), equalTo(true));
	}

	@Test
	public void phaseOfSynchronousEventTaskSuccessfullyConvertedToLogic() throws Exception {
		// given
		final org.cmdbuild.data.store.task.SynchronousEventTask afterCreate = a(org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "after_create") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeUpdate = a(org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "before_update") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask afterUpdate = a(org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "after_update") //
		);
		final org.cmdbuild.data.store.task.SynchronousEventTask beforeDelete = a(org.cmdbuild.data.store.task.SynchronousEventTask
				.newInstance() //
				.withParameter(SynchronousEvent.PHASE, "before_delete") //
		);

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
