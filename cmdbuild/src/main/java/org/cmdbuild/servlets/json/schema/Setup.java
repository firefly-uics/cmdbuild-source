package org.cmdbuild.servlets.json.schema;

import java.io.IOException;
import java.util.Map;

import org.cmdbuild.common.annotations.OldDao;
import org.cmdbuild.config.DefaultProperties;
import org.cmdbuild.exception.AuthException;
import org.cmdbuild.services.SessionVars;
import org.cmdbuild.services.Settings;
import org.cmdbuild.services.auth.UserContext;
import org.cmdbuild.servlets.json.JSONBase;
import org.cmdbuild.servlets.json.JSONBase.Admin.AdminAccess;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONException;
import org.json.JSONObject;

public class Setup extends JSONBase {

	@OldDao
	@JSONExported
	@Unauthorized
	public JSONObject getConfiguration(
			@Parameter("name") String nameOfConfigFile,
			JSONObject serializer,
			UserContext userCtx
		) throws JSONException, AuthException {
		DefaultProperties module = Settings.getInstance().getModule(nameOfConfigFile);
		boolean userIsAdmin = (userCtx != null && userCtx.privileges().isAdmin()); //FIXME with new dao
		JSONObject data = new JSONObject();
		for(Object keyObject : module.keySet()) {
			String key = keyObject.toString();
			if (userIsAdmin || !key.endsWith("password"))
				data.put(key, module.get(key));
		}
		serializer.put("data", data);
		return serializer;
	}

	@JSONExported
	@Admin(AdminAccess.DEMOSAFE)
	public void saveConfiguration (
			@Parameter("name") String nameOfConfigFile,
			Map<String,String> requestParams
		) throws IOException {
	    DefaultProperties module = Settings.getInstance().getModule(nameOfConfigFile);
	    for (Object keyObject : module.keySet()) {
			String key = keyObject.toString();
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
