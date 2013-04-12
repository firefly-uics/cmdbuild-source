package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.Map;

import org.cmdbuild.config.DefaultProperties;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.logic.TemporaryObjectsBeforeSpringDI;
import org.cmdbuild.services.Settings;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Setup extends JSONBaseWithSpringContext {

	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration(@Parameter("name") final String nameOfConfigFile, //
			final JSONObject serializer //
	) throws JSONException, AuthException {
		final DefaultProperties module = Settings.getInstance().getModule(nameOfConfigFile);
		final boolean userIsAdmin = operationUser().hasAdministratorPrivileges();
		final JSONObject data = new JSONObject();
		for (final Object keyObject : module.keySet()) {
			final String key = keyObject.toString();
			if (userIsAdmin || !key.endsWith("password")) {
				data.put(key, module.get(key));
			}
		}
		serializer.put("data", data);
		return serializer;
	}

	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveConfiguration(@Parameter("name") final String nameOfConfigFile,
			final Map<String, String> requestParams) throws IOException {
		final DefaultProperties module = Settings.getInstance().getModule(nameOfConfigFile);
		for (final Object keyObject : module.keySet()) {
			final String key = keyObject.toString();
			if (requestParams.containsKey(key)) {
				String value = requestParams.get(key);
				if (value == null) {
					value = "";
				}
				module.setProperty(key, value);
			}
		}
		module.store();
	}
}
