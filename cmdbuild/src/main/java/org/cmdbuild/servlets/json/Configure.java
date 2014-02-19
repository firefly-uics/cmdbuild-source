package org.cmdbuild.servlets.json;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.config.DatabaseConfiguration;
import org.cmdbuild.config.DatabaseProperties;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.auth.AuthenticationLogic;
import org.cmdbuild.logic.auth.GroupDTO;
import org.cmdbuild.logic.auth.UserDTO;
import org.cmdbuild.services.PatchManager.Patch;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.database.DatabaseConfigurator;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.postgresql.ds.PGSimpleDataSource;

public class Configure extends JSONBaseWithSpringContext {

	private static final String MANAGEMENT_DATABASE = "postgres";

	@JSONExported
	@Configuration
	@Unauthorized
	public void testConnection(@Parameter("host") final String host, @Parameter("port") final int port,
			@Parameter("user") final String user, @Parameter("password") final String password) {
		testDatabaseConnection(host, port, user, password);
	}

	private void testDatabaseConnection(final String host, final int port, final String username,
			final String plainPassword) {
		try {
			// FIXME hide implementation details somewhere else
			final PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setServerName(host);
			dataSource.setPortNumber(port);
			dataSource.setDatabaseName(MANAGEMENT_DATABASE);
			dataSource.setUser(username);
			dataSource.setPassword(plainPassword);
			dataSource.getConnection();
		} catch (final SQLException ex) {
			Log.CMDBUILD.info("Test connection failed: " + ex.getMessage());
			throw ORMExceptionType.ORM_DATABASE_CONNECTION_ERROR.createException();
		}
	}

	@JSONExported
	@Configuration
	@Unauthorized
	public void apply( //
			@Parameter("language") final String language, //
			@Parameter("language_prompt") final boolean languagePrompt, //
			@Parameter("db_type") final String dbType, //
			@Parameter("db_name") final String dbName, //
			@Parameter("host") final String host, //
			@Parameter("port") final int port, //
			@Parameter("shark_schema") final boolean createSharkSchema, //
			@Parameter("user") final String user, //
			@Parameter("password") final String password, //
			@Parameter(value = "user_type", required = false) final String systemUserType, //
			@Parameter(value = "lim_user", required = false) final String limitedUser, //
			@Parameter(value = "lim_password", required = false) final String limitedPassword, //
			@Parameter(value = "admin_user", required = false) final String adminUser, //
			@Parameter(value = "admin_password", required = false) final String adminPassword //
	) throws IOException, SQLException {
		testDatabaseConnection(host, port, user, password);
		final CmdbuildProperties cmdbuildProps = cmdbuildConfiguration();
		cmdbuildProps.setLanguage(language);
		cmdbuildProps.setLanguagePrompt(languagePrompt);
		cmdbuildProps.store();

		final DatabaseConfigurator.Configuration configuration = new DatabaseConfigurator.Configuration() {

			@Override
			public String getHost() {
				return host;
			}

			@Override
			public int getPort() {
				return port;
			}

			@Override
			public String getUser() {
				return user;
			}

			@Override
			public String getPassword() {
				return password;
			}

			@Override
			public String getDatabaseName() {
				return dbName;
			}

			@Override
			public String getDatabaseType() {
				return dbType;
			}

			@Override
			public boolean useLimitedUser() {
				return !(systemUserType == null || "superuser".equals(systemUserType));
			}

			@Override
			public String getLimitedUser() {
				return limitedUser;
			}

			@Override
			public String getLimitedUserPassword() {
				return limitedPassword;
			}

			@Override
			public boolean useSharkSchema() {
				return createSharkSchema;
			}

			@Override
			public String getSqlPath() {
				return Settings.getInstance().getRootPath() + "WEB-INF" + File.separator + "sql" + File.separator;
			}

		};
		final DatabaseConfiguration databaseConfiguration = DatabaseProperties.getInstance();
		final DatabaseConfigurator configurator = new DatabaseConfigurator(configuration, databaseConfiguration,
				patchManager());
		configurator.configureAndSaveSettings();

		if (DatabaseConfigurator.EMPTY_DBTYPE.equals(dbType)) {
			AuthenticationLogic authLogic = authLogic();
			final GroupDTO groupDto = GroupDTO.newInstance() //
					.withName("SuperUser") //
					.withAdminFlag(true) //
					.withDescription("SuperUser") //
					.build();
			final CMGroup superUserGroup = authLogic.createGroup(groupDto);
			final UserDTO userDto = UserDTO.newInstance() //
					.withUsername(adminUser) //
					.withPassword(adminPassword) //
					.build();
			final CMUser administrator = authLogic.createUser(userDto);
			authLogic.addUserToGroup(administrator.getId(), superUserGroup.getId());
		}
		patchManager().reset();
	}

	@JSONExported
	@Unauthorized
	public JSONObject getPatches(final JSONObject serializer) throws JSONException {
		final Iterable<Patch> avaiablePatches = patchManager().getAvaiblePatch();
		for (final Patch patch : avaiablePatches) {
			final JSONObject jsonPatch = new JSONObject();
			jsonPatch.put("name", patch.getVersion());
			jsonPatch.put("description", patch.getDescription());
			serializer.append("patches", jsonPatch);
		}
		return serializer;
	}

	@JSONExported
	@Unauthorized
	public JSONObject applyPatches(final JSONObject serializer) throws SQLException, Exception {
		patchManager().applyPatchList();
		// TODO should be database only
		cachingLogic().clearCache();
		return serializer;
	}

}
