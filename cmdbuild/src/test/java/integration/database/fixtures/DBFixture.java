package integration.database.fixtures;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.elements.database.DatabaseConfigurator;
import org.cmdbuild.elements.interfaces.ITable;
import org.cmdbuild.elements.interfaces.BaseSchema.CMTableType;
import org.cmdbuild.elements.interfaces.BaseSchema.Mode;
import org.cmdbuild.elements.interfaces.BaseSchema.SchemaStatus;
import org.cmdbuild.elements.interfaces.IAttribute.AttributeType;
import org.cmdbuild.elements.interfaces.IAttribute.FieldMode;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.Settings;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

public abstract class DBFixture {

	protected enum Mandatory {
		NULLABLE,
		NOTNULL;
		private boolean toBoolean() {
			return this == NOTNULL;
		}
	}

	protected enum Uniqueness {
		NOTUNIQUE,
		UNIQUE;
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
		Connection dbConnection = DBService.getConnection();
		dbConnection.setAutoCommit(false);
	}

	@After
	public final void rollbackAndReleaseTransaction() throws SQLException {
		rollbackTransaction();
		DBService.releaseConnection();
	}

	protected final void rollbackTransaction() throws SQLException {
		Connection dbConnection = DBService.getConnection();
		dbConnection.rollback();
	}

	@BeforeClass
	public static void setupConnectionAndDatabase() {
		String webRoot = System.getProperty("user.dir").concat("/src/main/webapp/"); // TODO
		Settings.getInstance().setRootPath(webRoot);
	    setupDatabaseProperties();
	    try {
	    	DBService.getConnection(testDBHost, testDBPort, testDBUser, testDBPassword, testDBName);
		} catch (SQLException e) {
			createDB();
		}
	}

	private static void setupDatabaseProperties() {
		DatabaseProperties dp = DatabaseProperties.getInstance();
	    dp.setDatabaseUrl(String.format("jdbc:postgresql://%1$s:%2$s/%3$s", testDBHost, testDBPort, testDBName));
	    dp.setDatabaseUser(testDBUser);
	    dp.setDatabasePassword(testDBPassword);
	}

	private static void createDB() {
		DatabaseConfigurator dbc = new DatabaseConfigurator();
		dbc.setHost(testDBHost);
		dbc.setPort(testDBPort);
		dbc.setUser(testDBSuperUser);
		dbc.setPassword(testDBSuperPassword);
		dbc.setDbName(testDBName);
		dbc.setDbType(DatabaseConfigurator.EMPTY_DBTYPE);
		dbc.setCreateLimitedUser(false);
		dbc.setCreateSharkSchema(false);
		dbc.setLimitedUser(testDBUser);
		dbc.setLimitedPassword(testDBPassword);
		dbc.configureAndDoNotSaveSettings();
	}

	protected final int createDBClass(String className) throws SQLException {
		return createDBClass(className, ITable.BaseTable);
	}

	protected final int createDBClass(String className, String parentClassName) throws SQLException {
		return createDBClass(className, parentClassName, false);
	}

	protected final int createDBSuperClass(String className) throws SQLException {
		return createDBSuperClass(className, ITable.BaseTable);
	}

	protected final int createDBSuperClass(String className, String parentClassName) throws SQLException {
		return createDBClass(className, parentClassName, true);
	}

	private final int createDBClass(String className, String parentClassName, boolean isSuperClass) throws SQLException {
		String classComment = createClassComment(Mode.WRITE, className+" Description", isSuperClass);
		return createDBClassWithComment(className, parentClassName, classComment);
	}

	protected final int createDBClassWithComment(String className, String parentClassName, String classComment)
			throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT cm_create_class(?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, parentClassName);
		stm.setString(3, classComment);
		ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected final int legacyCreateDBClassWithComment(String className, String parentClassName,
			boolean isSuperClass, String classComment) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT system_class_create(?, ?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, parentClassName);
		stm.setBoolean(3, isSuperClass);
		stm.setString(4, classComment);
		ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected final String createClassComment(Mode mode, String description, boolean isSuperClass) {
		return String.format("DESCR: %s|MODE: %s|STATUS: %s|SUPERCLASS: %b|TYPE: %s",
				mode.getModeString(), description, SchemaStatus.ACTIVE.commentString(),
				isSuperClass, CMTableType.CLASS.toMetaValue()
			);
	}

	protected int createDBDomain(DomainInfo domainInfo) throws SQLException {
		String domainComment = createDomainComment(domainInfo.getName()+" Description", domainInfo.getClass1(),
				domainInfo.getClass2(), domainInfo.getCardinality());
		return createDBDomainWithComment(domainInfo, domainComment);
	}

	protected int createDBDomainWithComment(DomainInfo domainInfo, String domainComment) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT cm_create_domain(?,?)");
		stm.setString(1, domainInfo.getName());
		stm.setString(2, domainComment);
		ResultSet rs = stm.executeQuery();
		rs.next();
		return rs.getInt(1);
	}

	protected String createDomainComment(String domainDescription, String class1Name, String class2Name, String cardinality) {
		return String.format(
				"CARDIN: %s|CLASS1: %s|CLASS2: %s|DESCRDIR: ->|DESCRINV: <-|LABEL: %s|MASTERDETAIL: %b|MODE: %s|STATUS: %s|TYPE: domain",
				cardinality, class1Name, class2Name, domainDescription, false, Mode.WRITE.getModeString(), SchemaStatus.ACTIVE.commentString()
			);
	}

	protected void createDBReference(String className, String referenceName, DomainInfo domain) throws SQLException {
		String attributeComment = createAttributeComment(referenceName+" Description",
				domain.getName(), domain.getReferenceDirection(), "restrict");
		createDBAttributeWithComment(className, referenceName, AttributeType.REFERENCE, attributeComment,
				domain.getReferenceTarget(), domain.getName(), "restrict", domain.getReferenceDirection());
	}

	private String createAttributeComment(String attributeDescription, String domainName, boolean direction,
			String referenceType) {
		String comment = String.format(
				"BASEDSP: %b|CLASSORDER: %d|DESCR: %s|FIELDMODE: %s|INDEX: %d|MODE: %s|STATUS: %s",
				false, 0, attributeDescription, FieldMode.WRITE.getMode(), 0,
				Mode.WRITE.getModeString(), SchemaStatus.ACTIVE.commentString()
			);
		if (domainName != null && !domainName.isEmpty()) {
			comment += String.format("|REFERENCEDIRECT: %b|REFERENCEDOM: %s|REFERENCETYPE: %s",
					direction, domainName, referenceType);
		}
		return comment;
	}

	protected void createDBAttributeWithComment(String className, String attributeName,
			AttributeType attributeType, String attributeComment,
			String domainTarget, String domainName, String referenceType, boolean domainDirection) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT cm_create_class_attribute(?, ?, ?, ?, ?, ?, ?)");
		stm.setString(1, className);
		stm.setString(2, attributeName);
		stm.setString(3, attributeType.toDBString());
		stm.setString(4, "NULL");            // attributedefault
		stm.setBoolean(5, Mandatory.NULLABLE.toBoolean());
		stm.setBoolean(6, Uniqueness.NOTUNIQUE.toBoolean());
		stm.setString(7, attributeComment);
		stm.execute();
	}

	protected void deleteDBClass(String className) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT cm_delete_class(?)");
		stm.setString(1, className);
		stm.execute();
	}

	protected void deleteDBAttribute(String className, String referenceName) throws SQLException {
		Connection conn = DBService.getConnection();
		CallableStatement stm = conn.prepareCall("SELECT cm_delete_class_attribute(?, ?)");
		stm.setString(1, className);
		stm.setString(2, referenceName);
		stm.execute();
	}
}
