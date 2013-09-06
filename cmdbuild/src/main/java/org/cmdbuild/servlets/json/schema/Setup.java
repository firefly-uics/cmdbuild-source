package org.cmdbuild.servlets.json.schema;

import static org.cmdbuild.servlets.json.ComunicationConstants.DATA;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.exception.AuthException;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.json.JSONBaseWithSpringContext;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Setup extends JSONBaseWithSpringContext {

	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration( //
			@Parameter(NAME) final String nameOfConfigFile //
	) throws JSONException, AuthException, Exception {
		final JSONObject out = new JSONObject();
		final JSONObject data = new JSONObject();
		for (final Entry<String, String> entry : setUpLogic().load(nameOfConfigFile).entrySet()) {
			data.put(entry.getKey(), entry.getValue());
		}
		out.put(DATA, data);
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

}