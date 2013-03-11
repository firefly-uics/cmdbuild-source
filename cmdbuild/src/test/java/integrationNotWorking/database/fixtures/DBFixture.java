package integrationNotWorking.database.fixtures;

import java.io.File;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.elements.database.DatabaseConfigurator;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.BaseSchema.SchemaStatus;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.IAttribute.FieldMode;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class DBFixture {

	protected enum Mandatory {
		NULLABLE, NOTNULL;
		private boolean toBoolean() {
			return this == NOTNULL;
		}
	}

	protected enum Uniqueness {
		NOTUNIQUE, UNIQUE;
		private boolean toBoolean() {
			return this == UNIQUE;
		}
	}

	private static final String testDBHost = "localhost";
	private static final int testDBPort = 10083;
	private static final String testDBSuperUser = "postgres";
	private static final String testDBSuperPassword = "postgres";
	private static final String testDBUser = "test";
	private static final String testDBPassword = "test";
	private static final String testDBName = "cmdbuild_test";

	@Before
	public final void unsetAutoCommit() throws SQLException {
		final Connection dbConnection = connection();
		dbConnection.setAutoCommit(false);
	}

	@After
	public final void rollbackAndReleaseTransaction() throws SQLException {
		rollbackTransaction();
		DBService.releaseConnection();
	}

	protected final void rollbackTransaction() throws SQLException {
		final Connection dbConnection = connection();
		dbConnection.rollback();
	}

	@BeforeClass
	public static void setupConnectionAndDatabase() {
		final String webRoot = System.getProperty("user.dir").concat("/src/main/webapp/"); // TODO
		Settings.getInstance().setRootPath(webRoot);
		setupDatabaseProperties();
		try {
			DBService.getConnection(testDBHost, testDBPort, testDBUser, testDBPassword, testDBName);
		} catch (final SQLException e) {
			createDB();
		}
	}

	private static void setupDatabaseProperties() {
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.setDatabaseUrl(String.format("jdbc:postgresql://%1$s:%2$s/%3$s", testDBHost, testDBPort, testDBName));
		dp.setDatabaseUser(testDBUser);
		dp.setDatabasePassword(testDBPassword);
	}

	private static void createDB() {
		final DatabaseConfigurator.Configuration configuration = new DatabaseConfigurator.Configuration() {

			@Override
			public String getHost() {
				return testDBHost;
			}

			@Override
			public int getPort() {
				return testDBPort;
			}

			@Override
			public String getUser() {
				return testDBSuperUser;
			}

			@Override
			public String getPassword() {
				return testDBSuperPassword;
			}

			@Override
			public String getDatabaseName() {
				return testDBName;
			}

			@Override
			public String getDatabaseType() {
				return DatabaseConfigurator.EMPTY_DBTYPE;
			}

			@Override
			public boolean useLimitedUser() {
				return false;
			}

			@Override
			public String getLimitedUser() {
				return testDBUser;
			}

			@Override
			public String getLimitedUserPassword() {
				return testDBPassword;
			}

			@Override
			public boolean useSharkSchema() {
				return false;
			}

			@Override
			public String getSqlPath() {
				return Settings.getInstance().getRootPath() + "WEB-INF" + File.separator + "sql" + File.separator;
			}

		};
		final DatabaseConfigurator dbc = new DatabaseConfigurator(configuration);
		dbc.configureAndDoNotSaveSettings();
	}

	protected final int createDBClass(final String className) throws SQLException {
		return createDBClass(className, ITable.BaseTable);
	}

	protected final int createDBClass(final String className, final String parentClassName) throws SQLException {
		return createDBClass(className, parentClassName, false);
	}

	protected final int createDBSuperClass(final String className) throws SQLException {
		return createDBSuperClass(className, ITable.BaseTable);
	}

	protected final int createDBSuperClass(final String className, final String parentClassName) throws SQLException {
		return createDBClass(className, parentClassName, true);
	}

	private final int createDBClass(final String className, final String parentClassName, final boolean isSuperClass)
			throws SQLException {
		final String classComment = createClassComment(Mode.WRITE, className + " Description", isSuperClass);
		return createDBClassWithComment(className, parentClassName, classComment);
	}

	protected final int createDBClassWithComment(final String className, final String parentClassName,
			final String classComment) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_create_class(?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, parentClassName);
		stm.setString(3, classComment);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected final int legacyCreateDBClassWithComment(final String className, final String parentClassName,
			final boolean isSuperClass, final String classComment) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT system_class_create(?, ?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, parentClassName);
		stm.setBoolean(3, isSuperClass);
		stm.setString(4, classComment);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected final String createClassComment(final Mode mode, final String description, final boolean isSuperClass) {
		return String.format("DESCR: %s|MODE: %s|STATUS: %s|SUPERCLASS: %b|TYPE: %s", mode.getModeString(),
				description, SchemaStatus.ACTIVE.commentString(), isSuperClass, CMTableType.CLASS.toMetaValue());
	}

	protected int createDBDomain(final DomainInfo domainInfo) throws SQLException {
		final String domainComment = createDomainComment(domainInfo.getName() + " Description", domainInfo.getClass1(),
				domainInfo.getClass2(), domainInfo.getCardinality());
		return createDBDomainWithComment(domainInfo, domainComment);
	}

	protected int createDBDomainWithComment(final DomainInfo domainInfo, final String domainComment)
			throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_create_domain(?,?)");
		stm.setString(1, domainInfo.getName());
		stm.setString(2, domainComment);
		final ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected String createDomainComment(final String domainDescription, final String class1Name,
			final String class2Name, final String cardinality) {
		return String
				.format("CARDIN: %s|CLASS1: %s|CLASS2: %s|DESCRDIR: ->|DESCRINV: <-|LABEL: %s|MASTERDETAIL: %b|MODE: %s|STATUS: %s|TYPE: domain",
						cardinality, class1Name, class2Name, domainDescription, false, Mode.WRITE.getModeString(),
						SchemaStatus.ACTIVE.commentString());
	}

	protected void createDBReference(final String className, final String referenceName, final DomainInfo domain)
			throws SQLException {
		final String attributeComment = createAttributeComment(referenceName + " Description", domain.getName(),
				domain.getReferenceDirection(), "restrict");
		createDBAttributeWithComment(className, referenceName, AttributeType.REFERENCE, attributeComment,
				domain.getReferenceTarget(), domain.getName(), "restrict", domain.getReferenceDirection());
	}

	private String createAttributeComment(final String attributeDescription, final String domainName,
			final boolean direction, final String referenceType) {
		String comment = String.format(
				"BASEDSP: %b|CLASSORDER: %d|DESCR: %s|FIELDMODE: %s|INDEX: %d|MODE: %s|STATUS: %s", false, 0,
				attributeDescription, FieldMode.WRITE.getMode(), 0, Mode.WRITE.getModeString(),
				SchemaStatus.ACTIVE.commentString());
		if (domainName != null && !domainName.isEmpty()) {
			comment += String.format("|REFERENCEDIRECT: %b|REFERENCEDOM: %s|REFERENCETYPE: %s", direction, domainName,
					referenceType);
		}
		return comment;
	}

	protected void createDBAttributeWithComment(final String className, final String attributeName,
			final AttributeType attributeType, final String attributeComment, final String domainTarget,
			final String domainName, final String referenceType, final boolean domainDirection) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_create_class_attribute(?, ?, ?, ?, ?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, attributeName);
		stm.setString(3, attributeType.toDBString());
		stm.setString(4, "NULL"); // attributedefault
		stm.setBoolean(5, Mandatory.NULLABLE.toBoolean());
		stm.setBoolean(6, Uniqueness.NOTUNIQUE.toBoolean());
		stm.setString(7, attributeComment);
		stm.execute();
	}

	protected void deleteDBClass(final String className) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_delete_class(?)");
		stm.setString(1, className);
		stm.execute();
	}

	protected void deleteDBDomain(final DomainInfo d) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_delete_domain(?)");
		stm.setString(1, d.getName());
		stm.execute();
	}

	protected void deleteDBAttribute(final String className, final String referenceName) throws SQLException {
		final Connection conn = connection();
		final CallableStatement stm = conn.prepareCall("SELECT cm_delete_class_attribute(?, ?)");
		stm.setString(1, className);
		stm.setString(2, referenceName);
		stm.execute();
	}

	protected Connection connection() {
		return DBService.getConnection();
	}

}
