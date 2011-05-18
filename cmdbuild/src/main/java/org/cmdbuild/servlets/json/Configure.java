package org.cmdbuild.servlets.json;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;

import org.cmdbuild.config.CmdbuildProperties;
import org.cmdbuild.elements.database.DatabaseConfigurator;
import org.cmdbuild.exception.ORMException.ORMExceptionType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.services.DBService;
import org.cmdbuild.services.PatchManager;
import org.cmdbuild.services.PatchManager.Patch;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Configure extends JSONBase {

	@JSONExported
	@Configuration
	@Unauthorized
	public void testConnection(
			@Parameter("host") String host,
			@Parameter("port") int port,
			@Parameter("user") String user,
			@Parameter("password") String password ) {
		try {
			DBService.getConnection(host, port, user, password);
		} catch (SQLException e) {
			Log.OTHER.info("Test connection failed: " + e.getMessage());
			throw ORMExceptionType.ORM_DATABASE_CONNECTION_ERROR.createException();
		}
	}

	@JSONExported
	@Configuration
	@Unauthorized
	public void apply(
			@Parameter("language") String language,
			@Parameter("language_prompt") boolean languagePrompt,
			@Parameter("db_type") String dbType,
			@Parameter("db_name") String dbName,
			@Parameter("host") String host,
			@Parameter("port") int port,
			@Parameter("shark_schema") boolean createSharkSchema,
			@Parameter("user") String user,
			@Parameter("password") String password,
			@Parameter(value="user_type", required=false) String systemUserType,
			@Parameter(value="lim_user", required=false) String limitedUser,
			@Parameter(value="lim_password", required=false) String limitedPassword,
			@Parameter(value="admin_user", required=false) String adminUser,
			@Parameter(value="admin_password", required=false) String adminPassword
		)	throws IOException, SQLException {
		DatabaseConfigurator configurator = new DatabaseConfigurator();
		configurator.setDbType(dbType);
		configurator.setDbName(dbName);
		configurator.setHost(host);
		configurator.setPort(port);
		configurator.setCreateSharkSchema(createSharkSchema);
		configurator.setUser(user);
		configurator.setPassword(password);
		
		CmdbuildProperties cmdbuildProps = CmdbuildProperties.getInstance();
		cmdbuildProps.setLanguage(language);
		cmdbuildProps.setLanguagePrompt(languagePrompt);
		cmdbuildProps.store();
		
		if (systemUserType == null || "superuser".equals(systemUserType)) {
			configurator.setCreateLimitedUser(false);
		} else {
			configurator.setCreateLimitedUser("new_limuser".equals(systemUserType));
			configurator.setLimitedUser(limitedUser);
			configurator.setLimitedPassword(limitedPassword);
		}
		configurator.configureAndSaveSettings();
		configurator.createAdministratorIfNeeded(adminUser, adminPassword);
	}
	
	@JSONExported
	@Unauthorized
	public JSONObject getPatches(	JSONObject serializer ) throws JSONException {
		LinkedList<Patch> avaiablePatches = PatchManager.getInstance().getAvaiblePatch();
		for (Patch patch: avaiablePatches) {
			JSONObject jsonPatch = new JSONObject();
			jsonPatch.put("name", patch.getVersion());
			jsonPatch.put("description", patch.getDescription());
			serializer.append("patches", jsonPatch);
		}
		return serializer;
	}
	
	@JSONExported
	@Unauthorized
	public JSONObject applyPatches( JSONObject serializer ) throws SQLException, Exception {
		PatchManager.getInstance().applyPatchList();
		return serializer;
	}
}
