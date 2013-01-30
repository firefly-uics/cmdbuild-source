package org.cmdbuild.elements.database;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.elements.interfaces.IDomain;
import org.cmdbuild.elements.interfaces.IRelation;
import org.cmdbuild.elements.wrappers.GroupCard;
import org.cmdbuild.elements.wrappers.UserCard;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.services.auth.UserOperations;
import org.cmdbuild.utils.FileUtils;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseConfigurator {

	public interface Configuration {

		String getHost();

		int getPort();

		String getUser();

		String getPassword();

		String getDatabaseName();

		String getDatabaseType();

		boolean useLimitedUser();

		String getLimitedUser();

		String getLimitedUserPassword();

		boolean useSharkSchema();

		String getSqlPath();

	}

	private static final String POSTGRES_SUPER_DATABASE = "postgres";
	private static final String SQL_STATE_FOR_ALREADY_PRESENT_ELEMENT = "42710";

	private static final String EXISTING_DBTYPE = "existing";
	// TODO make it private
	public static final String EMPTY_DBTYPE = "empty";
	private static final String SHARK_PASSWORD = "shark";
	private static final String SHARK_USERNAME = "shark";
	private static final String SHARK_SCHEMA = "shark";

	private static String CREATE_LANGUAGE = "CREATE LANGUAGE plpgsql";
	private static String CREATE_DATABASE = "CREATE DATABASE \"%s\" ENCODING = 'UTF8'";
	private static String CREATE_ROLE = "CREATE ROLE \"%s\" NOSUPERUSER NOCREATEDB NOCREATEROLE INHERIT LOGIN ENCRYPTED PASSWORD '%s'";
	private static String CREATE_SCHEMA = "CREATE SCHEMA %s";
	private static String ALTER_DATABASE_OWNER = "ALTER DATABASE \"%s\" OWNER TO \"%s\"";
	private static String GRANT_SCHEMA_PRIVILEGES = "GRANT ALL ON SCHEMA \"%s\" TO \"%s\"";
	private static String ALTER_ROLE_PATH = "ALTER ROLE \"%s\" SET search_path=%s";

	private final String baseSqlPath;
	private final String sampleSqlPath;
	private final String sharkSqlPath;

	private final Configuration configuration;

	public DatabaseConfigurator(final Configuration configuration) {
		this.configuration = configuration;
		baseSqlPath = configuration.getSqlPath() + "base_schema" + File.separator;
		sampleSqlPath = configuration.getSqlPath() + "sample_schemas" + File.separator;
		sharkSqlPath = configuration.getSqlPath() + "shark_schema" + File.separator;
	}

	public DataSource superDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(configuration.getUser());
		dataSource.setPassword(configuration.getPassword());
		dataSource.setDatabaseName(POSTGRES_SUPER_DATABASE);
		return dataSource;
	}

	public DataSource systemDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(getSystemUser());
		dataSource.setPassword(getSystemPassword());
		dataSource.setDatabaseName(configuration.getDatabaseName());
		return dataSource;
	}

	private DataSource sharkDataSource() {
		final PGSimpleDataSource dataSource = new PGSimpleDataSource();
		dataSource.setServerName(configuration.getHost());
		dataSource.setPortNumber(configuration.getPort());
		dataSource.setUser(SHARK_USERNAME);
		dataSource.setPassword(SHARK_PASSWORD);
		dataSource.setDatabaseName(configuration.getDatabaseName());
		return dataSource;
	}

	private boolean needsLimitedUser() {
		return (configuration.getLimitedUser() != null) && (configuration.getLimitedUserPassword() != null);
	}

	private String getSystemUser() {
		if (needsLimitedUser()) {
			return configuration.getLimitedUser();
		} else {
			return configuration.getUser();
		}
	}

	private String getSystemPassword() {
		if (needsLimitedUser()) {
			return configuration.getLimitedUserPassword();
		} else {
			return configuration.getPassword();
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

	private void configure(final boolean saveSettings) {
		try {
			prepareConfiguration();
			createDatabaseIfNeeded();
			fillDatabaseIfNeeded();
			addLastPatchIfEmptyDb();
			if (saveSettings) {
				saveConfiguration();
			}
		} catch (final Exception e) {
			clearConfiguration();
			throw ORMExceptionType.ORM_GENERIC_ERROR.createException();
		}
	}

	private void addLastPatchIfEmptyDb() {
		if (EMPTY_DBTYPE.equals(configuration.getDatabaseType())) {
			PatchManager.getInstance().createLastPatch();
		}
	}

	private void createDatabaseIfNeeded() {
		if (!EXISTING_DBTYPE.equals(configuration.getDatabaseType())) {
			createDatabase(configuration.getDatabaseName());
		}
	}

	private void createDatabase(final String name) {
		Log.OTHER.info("Creating database");
		new JdbcTemplate(superDataSource()).execute(String.format(CREATE_DATABASE, escapeSchemaName(name)));
	}

	private void createPLSQLLanguage() {
		Log.OTHER.info("Creating PL/SQL language");
		try {
			new JdbcTemplate(superDataSource()).execute(CREATE_LANGUAGE);
		} catch (final DataAccessException e) {
			forwardIfNotAlreadyPresentElement(e);
		}
	}

	private void fillDatabaseIfNeeded() {
		if (!EXISTING_DBTYPE.equals(configuration.getDatabaseType())) {
			createSystemRoleIfNeeded();
			alterDatabaseOwnerIfNeeded();
			createPLSQLLanguage();
			if (EMPTY_DBTYPE.equals(configuration.getDatabaseType())) {
				createCmdbuildStructure();
			} else {
				restoreSampleDB();
			}
			if (configuration.useSharkSchema()) {
				createSharkRole();
				createSchema(SHARK_SCHEMA);
				grantSchemaPrivileges(SHARK_SCHEMA, SHARK_USERNAME);
				createSharkTables();
			}
		}
	}

	private void alterDatabaseOwnerIfNeeded() {
		if (needsLimitedUser())
			alterDatabaseOwner(configuration.getDatabaseName(), getSystemUser());
	}

	private void restoreSampleDB() {
		Log.OTHER.info("Restoring demo structure");
		final String filename = sampleSqlPath + configuration.getDatabaseType() + "_schema.sql";
		final String sql = FileUtils.getContents(filename);
		new JdbcTemplate(systemDataSource()).execute(sql);
	}

	private void createSharkTables() {
		Log.OTHER.info("Creating shark tables");
		new JdbcTemplate(sharkDataSource()).execute(FileUtils.getContents(sharkSqlPath + "02_shark_emptydb.sql"));
	}

	private void createSchema(final String schema) {
		new JdbcTemplate(systemDataSource()).execute(String.format(CREATE_SCHEMA, escapeSchemaName(schema)));
	}

	private void createCmdbuildStructure() {
		Log.OTHER.info("Creating CMDBuild structure");
		final JdbcTemplate jdbcTemplate = new JdbcTemplate(systemDataSource());
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "01_system_functions_base.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "02_system_functions_class.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "03_system_functions_attribute.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "04_system_functions_domain.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "05_base_tables.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "06_system_views_base.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "07_support_tables.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "08_user_tables.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "09_system_views_extras.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "10_system_functions_extras.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "11_workflow.sql"));
		jdbcTemplate.execute(FileUtils.getContents(baseSqlPath + "12_tecnoteca_extras.sql"));
	}

	private void createSystemRoleIfNeeded() {
		if (configuration.useLimitedUser())
			createRole(configuration.getLimitedUser(), configuration.getLimitedUserPassword());
	}

	private void alterDatabaseOwner(final String database, final String role) {
		Log.OTHER.info("Changing database ownership");
		new JdbcTemplate(superDataSource()).execute(String.format(ALTER_DATABASE_OWNER, escapeSchemaName(database),
				escapeSchemaName(role)));
	}

	private void grantSchemaPrivileges(final String schema, final String role) {
		Log.OTHER.info("Granting schema privileges");
		new JdbcTemplate(systemDataSource()).execute(String.format(GRANT_SCHEMA_PRIVILEGES, escapeSchemaName(schema),
				escapeSchemaName(role)));
	}

	private void createRole(final String roleName, final String rolePassword) {
		Log.OTHER.info("Creating role " + roleName);
		new JdbcTemplate(superDataSource()).execute(String.format(CREATE_ROLE, escapeSchemaName(roleName),
				escapeValue(rolePassword)));
	}

	private void createSharkRole() {
		Log.OTHER.info("Creating shark role");
		try {
			final JdbcTemplate jdbcTemplate = new JdbcTemplate(superDataSource());
			jdbcTemplate.execute(String.format(CREATE_ROLE, SHARK_USERNAME, SHARK_PASSWORD));
			jdbcTemplate.execute(String.format(ALTER_ROLE_PATH, SHARK_USERNAME, "pg_default,shark"));
		} catch (final DataAccessException e) {
			forwardIfNotAlreadyPresentElement(e);
		}
	}

	private void prepareConfiguration() throws IOException {
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.setDatabaseUrl(String.format("jdbc:postgresql://%1$s:%2$s/%3$s", configuration.getHost(),
				configuration.getPort(), configuration.getDatabaseName()));
		dp.setDatabaseUser(getSystemUser());
		dp.setDatabasePassword(getSystemPassword());
	}

	private void saveConfiguration() throws IOException {
		Log.OTHER.info("Saving configuration");
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.store();
	}

	private void clearConfiguration() {
		final DatabaseProperties dp = DatabaseProperties.getInstance();
		dp.clearConfiguration();
	}

	/*
	 * NOTE: It MUST be called after the database has been configured, otherwise
	 * it will fail
	 */
	@OldDao
	public void createAdministratorIfNeeded(final String adminUser, final String adminPassword) {
		if (EMPTY_DBTYPE.equals(configuration.getDatabaseType())) {
			final UserCard user = new UserCard();
			user.setUsername(adminUser);
			user.setUnencryptedPassword(adminPassword);
			user.setDescription("Administrator");
			user.save();
			final GroupCard role = new GroupCard();
			role.setIsAdmin(true);
			role.setName("SuperUser");
			role.setDescription("SuperUser");
			role.save();
			final UserContext systemCtx = UserContext.systemContext();
			final IDomain userRoleDomain = UserOperations.from(systemCtx).domains()
					.get(AuthenticationLogic.USER_GROUP_DOMAIN_NAME);
			final IRelation relation = UserOperations.from(systemCtx).relations().create(userRoleDomain, user, role);
			relation.save();
		}
	}

	/*
	 * We don't know what could go wrong if this is allowed
	 */
	private String escapeSchemaName(final String name) {
		if (name.indexOf('"') >= 0) {
			throw ORMExceptionType.ORM_ILLEGAL_NAME_ERROR.createException(name);
		}
		return name;
	}

	private String escapeValue(final String value) {
		return value.replaceAll("'", "''");
	}

	private void forwardIfNotAlreadyPresentElement(final DataAccessException e) {
		final Throwable cause = e.getCause();
		if (cause instanceof SQLException) {
			final String sqlState = SQLException.class.cast(cause).getSQLState();
			if (!SQL_STATE_FOR_ALREADY_PRESENT_ELEMENT.equals(sqlState)) {
				throw e;
			} else {
				// TODO log
			}
		} else {
			throw e;
		}
	}

}
