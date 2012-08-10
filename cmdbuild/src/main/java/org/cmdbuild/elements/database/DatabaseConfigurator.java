package org.cmdbuild.elements.database;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.auth.AuthenticationFacade;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.utils.FileUtils;

public class DatabaseConfigurator {

	public static final String EXISTING_DBTYPE = "existing";
	public static final String EMPTY_DBTYPE = "empty";
	static final String SHARK_PASSWORD = "shark";
	static final String SHARK_USERNAME = "shark";
	static final String SHARK_SCHEMA = "shark";

	private static String CREATE_LANGUAGE = "CREATE LANGUAGE plpgsql";
	private static String CREATE_DATABASE = "CREATE DATABASE \"%s\" ENCODING = 'UTF8'";
	private static String CREATE_ROLE = "CREATE ROLE \"%s\" NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN ENCRYPTED PASSWORD '%s'";
	private static String CREATE_SCHEMA = "CREATE SCHEMA %s";
	private static String ALTER_DATABASE_OWNER = "ALTER DATABASE \"%s\" OWNER TO \"%s\"";
	private static String GRANT_SCHEMA_PRIVILEGES = "GRANT ALL ON SCHEMA \"%s\" TO \"%s\"";
	private static String ALTER_ROLE_PATH = "ALTER ROLE \"%s\" SET search_path=%s"; 

	Connection superConnection;
	Connection systemConnection;
	Connection sharkConnection;

	private String dbType;
	private String dbName;
	private String host;
	private int port;
	private boolean createSharkSchema;
	private String user;
	private String password;
	private boolean createLimitedUser;
	private String limitedUser;
	private String limitedPassword;

	private final String baseSqlPath;
	private final String sampleSqlPath;
	private final String sharkSqlPath;

	/*
	 * Setters
	 */

	public DatabaseConfigurator() {
		super();
		String sqlPath = Settings.getInstance().getRootPath() +
			"WEB-INF" + File.separator + "sql" + File.separator;
		baseSqlPath = sqlPath + "base_schema" + File.separator;
		sampleSqlPath = sqlPath + "sample_schemas" + File.separator;
		sharkSqlPath = sqlPath + "shark_schema" + File.separator;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setCreateSharkSchema(boolean createSharkSchema) {
		this.createSharkSchema = createSharkSchema;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setCreateLimitedUser(boolean createLimitedUser) {
		this.createLimitedUser = createLimitedUser;
	}

	public void setLimitedUser(String limitedUser) {
		this.limitedUser = limitedUser;
	}

	public void setLimitedPassword(String limitedPassword) {
		this.limitedPassword = limitedPassword;
	}

	/*
	 * Handle connections
	 */

	private Connection getSuperConnection() throws SQLException {
		if (superConnection == null)
			superConnection = DBService.getConnection(host, port, user, password);
		return superConnection;
	}

	private Connection getSystemConnection() throws SQLException {
		if (systemConnection == null) {
			systemConnection = DBService.getConnection(host, port, getSystemUser(), getSystemPassword(), dbName);
		}
		return systemConnection;
	}

	private boolean needsLimitedUser() {
		return (limitedUser != null) && (limitedPassword != null);
	}

	private String getSystemUser() {
		if (needsLimitedUser())
			return limitedUser;
		else
			return user;
	}

	private String getSystemPassword() {
		if (needsLimitedUser())
			return limitedPassword;
		else
			return password;
	}

	private Connection getSharkConnection() throws SQLException {
		if (sharkConnection == null)
			sharkConnection = DBService.getConnection(host, port, SHARK_USERNAME, SHARK_PASSWORD, dbName);
		return sharkConnection;
	}

	private void closeOpenedConnections() {
		if (superConnection != null) {
			try {
				superConnection.close();
			} catch (SQLException e) {
				Log.SQL.error("Error closing super connection: " + e.getMessage());
			}
		}
		if (systemConnection != null) {
			try {
				systemConnection.close();
			} catch (SQLException e) {
				Log.SQL.error("Error closing system connection: " + e.getMessage());
			}
		}
		if (sharkConnection != null) {
			try {
				sharkConnection.close();
			} catch (SQLException e) {
				Log.SQL.error("Error closing Shark connection: " + e.getMessage());
			}
		}
	}

	/*
	 * Configure
	 */

	public void configureAndSaveSettings() {
		configure(true);
	}

	public void configureAndDoNotSaveSettings() {
		configure(false);
	}

	private void configure(boolean saveSettings) {
		try {
			prepareConfiguration();
			createDatabaseIfNeeded();
			fillDatabaseIfNeeded();
			addLastPatchIfEmptyDb();
			if (saveSettings) {
				saveConfiguration();
			}
		} catch (Exception e) {
			clearConfiguration();
			if (e instanceof SQLException) {
				throw ORMExceptionType.ORM_ERROR_GENERIC_SQL.createException(e.getMessage());
			} else {
				throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
			}
		} finally {
			closeOpenedConnections();
		}
	}

	private void addLastPatchIfEmptyDb() {
		if (EMPTY_DBTYPE.equals(dbType)) {
			PatchManager.getInstance().createLastPatch();
		}
	}
	
	private void createDatabaseIfNeeded() throws SQLException {
		if (!EXISTING_DBTYPE.equals(dbType)) {
			createDatabase(dbName);
		}
	}

	private void createDatabase(String name) throws SQLException {
		Log.OTHER.info("Creating database");
		Statement stm = getSuperConnection().createStatement();
		try {
			stm.execute(String.format(CREATE_DATABASE, escapeSchemaName(name)));
		} finally {
			stm.close();
		}
	}

	private void createPLSQLLanguage() throws SQLException {
		Log.OTHER.info("Creating PL/SQL language");
		Statement stm = getSystemConnection().createStatement();
		try { 
			stm.execute(CREATE_LANGUAGE);
		} catch(SQLException e){
			Log.SQL.warn("Cannot create PL/SQL language. Present already?");
		} finally {
			stm.close();
		}
	}

	private void fillDatabaseIfNeeded() throws SQLException {
		if (!EXISTING_DBTYPE.equals(dbType)) {
			createSystemRoleIfNeeded();
			alterDatabaseOwnerIfNeeded();
			createPLSQLLanguage();
			if (EMPTY_DBTYPE.equals(dbType)) {
				createCmdbuildStructure();
			} else {
				restoreSampleDB();
			}
			if (createSharkSchema) {
				createSharkRole();
				createSchema(SHARK_SCHEMA);
				grantSchemaPrivileges(SHARK_SCHEMA, SHARK_USERNAME);
				createSharkTables();
			}
		}
	}

	private void alterDatabaseOwnerIfNeeded() throws SQLException {
		if (needsLimitedUser())
			alterDatabaseOwner(dbName, getSystemUser());
	}

	private void restoreSampleDB() throws SQLException {
		Log.OTHER.info("Restoring demo structure");
		Statement stm = getSystemConnection().createStatement();
		try {
			stm.execute(FileUtils.getContents(sampleSqlPath + dbType + "_schema.sql"));
		} finally {
			stm.close();
		}
	}

	private void createSharkTables() throws SQLException {
		Log.OTHER.info("Creating shark tables");
		Statement stm = getSharkConnection().createStatement();
		try {
			stm.execute(FileUtils.getContents(sharkSqlPath + "02_shark_emptydb.sql"));
		} finally {
			stm.close();
		}
	}

	private void createSchema(String schema) throws SQLException {
		Log.OTHER.info("Creating schema " + schema);
		Statement stm = getSystemConnection().createStatement();
		try {
			stm.execute(String.format(CREATE_SCHEMA, escapeSchemaName(schema)));
		} finally {
			stm.close();
		}
	}

	private void createCmdbuildStructure() throws SQLException {
		Log.OTHER.info("Creating CMDBuild structure");
		Statement stm = getSystemConnection().createStatement();
		try {
			stm.execute(FileUtils.getContents(baseSqlPath + "01_system_functions_base.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "02_system_functions_class.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "03_system_functions_attribute.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "04_system_functions_domain.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "05_base_tables.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "06_system_views_base.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "07_support_tables.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "08_user_tables.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "09_system_views_extras.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "10_system_functions_extras.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "11_workflow.sql"));
			stm.execute(FileUtils.getContents(baseSqlPath + "12_tecnoteca_extras.sql"));
		} finally {
			stm.close();
		}
	}

	private void createSystemRoleIfNeeded() throws SQLException {
		if (createLimitedUser)
			createRole(limitedUser, limitedPassword);
	}

	private void alterDatabaseOwner(String database, String role) throws SQLException {
		Log.OTHER.info("Changing database ownership");
		Statement stm = getSuperConnection().createStatement();
		try {
			stm.execute(String.format(ALTER_DATABASE_OWNER,
					escapeSchemaName(database),
					escapeSchemaName(role)));
		} finally {
			stm.close();
		}
	}

	private void grantSchemaPrivileges(String schema, String role) throws SQLException {
		Log.OTHER.info("Granting schema privileges");
		Statement stm = getSystemConnection().createStatement();
		try {
			stm.execute(String.format(GRANT_SCHEMA_PRIVILEGES,
					escapeSchemaName(schema),
					escapeSchemaName(role)));
		} finally {
			stm.close();
		}
	}

	private void createRole(String roleName, String rolePassword) throws SQLException {
		Log.OTHER.info("Creating role " + roleName);
		Statement stm = getSuperConnection().createStatement();
		try {
			stm.execute(String.format(CREATE_ROLE, escapeSchemaName(roleName), escapeValue(rolePassword)));
		} finally {
			stm.close();
		}
	}

	private void createSharkRole() throws SQLException {
		Log.OTHER.info("Creating shark role");
		Statement stm = getSuperConnection().createStatement();
		try {
			stm.execute(String.format(CREATE_ROLE, SHARK_USERNAME, SHARK_PASSWORD));
			stm.execute(String.format(ALTER_ROLE_PATH, SHARK_USERNAME, "pg_default,shark"));
		} catch (SQLException e) {
			// We don't care if the user shark already exists
			if (!"42710".equals(e.getSQLState()))
				throw e;
		} finally {
			stm.close();
		}
	}

	private void prepareConfiguration() throws IOException {
	    DatabaseProperties dp = DatabaseProperties.getInstance();
	    dp.setDatabaseUrl(String.format("jdbc:postgresql://%1$s:%2$s/%3$s", host, port, dbName));
	    dp.setDatabaseUser(getSystemUser());
	    dp.setDatabasePassword(getSystemPassword());
	}

	private void saveConfiguration() throws IOException {
		Log.OTHER.info("Saving configuration");
	    DatabaseProperties dp = DatabaseProperties.getInstance();
	    dp.store();
	}

	private void clearConfiguration() {
		DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.clearConfiguration();
	}

	/*
	 * NOTE: It MUST be called after the database has been configured, otherwise it will fail
	 */
	public void createAdministratorIfNeeded(String adminUser, String adminPassword) {
		if (EMPTY_DBTYPE.equals(dbType)) {
			UserCard user = new UserCard();
			user.setUsername(adminUser);
			user.setUnencryptedPassword(adminPassword);
			user.setDescription("Administrator");
			user.save();
			GroupCard role = new GroupCard();
			role.setIsAdmin(true);
			role.setName("SuperUser");
			role.setDescription("SuperUser");
			role.save();
			UserContext systemCtx = UserContext.systemContext();
			IDomain userRoleDomain = systemCtx.domains().get(AuthenticationFacade.USER_GROUP_DOMAIN_NAME);
			IRelation relation = systemCtx.relations().create(userRoleDomain, user, role);
			relation.save();
		}
    }

	/*
	 * We don't know what could go wrong if this is allowed
	 */
	private String escapeSchemaName(String name) {
		if (name.indexOf('"') >= 0)
			throw ORMExceptionType.ORM_ILLEGAL_NAME_ERROR.createException(name);
		return name;
	}

	private String escapeValue(String value) {
		return value.replaceAll("'", "''");
	}
}
