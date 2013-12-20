package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.DATA;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAMES;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.codehaus.jettison.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Setup extends JSONBaseWithSpringContext {

	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration( //
			@Parameter(NAME) final String nameOfConfigFile //
	) throws JSONException, AuthException, Exception {
		final JSONObject out = new JSONObject();
		out.put(DATA, readConfig(nameOfConfigFile));

		return out;
	}

	@JSONExported
	@Unauthorized
	public JSONObject getConfigurations( //
			@Parameter(NAMES) final String namesOfConfigFileString //
			) throws JSONException, AuthException, Exception {
		final JSONObject out = new JSONObject();
		final JSONArray namesOfConfigFile = new JSONArray(namesOfConfigFileString);
		for (int i = 0, l = namesOfConfigFile.length(); i<l; i++) {
			final Object nameAsObject = namesOfConfigFile.get(i);
			final String name = (String)nameAsObject;
			out.put(name, readConfig(name));
		}

		return out;
	}

	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveConfiguration( //
			@Parameter(NAME) final String nameOfConfigFile, //
			final Map<String, String> requestParams //
	) throws Exception {
		setUpLogic().save(nameOfConfigFile, requestParams);
	}

	private JSONObject readConfig(final String nameOfConfigFile)
			throws Exception, JSONException {
		final JSONObject data = new JSONObject();
		for (final Entry<String, String> entry : setUpLogic().load(nameOfConfigFile).entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}
		return data;
	}
}