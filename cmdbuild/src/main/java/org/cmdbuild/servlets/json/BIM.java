package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_PROJECTS;
import static org.cmdbuild.servlets.json.ComunicationConstants.LIMIT;
import static org.cmdbuild.servlets.json.ComunicationConstants.START;

import java.util.List;

import org.cmdbuild.exception.CMDBException;
import org.cmdbuild.model.bim.BIMProject;
import org.cmdbuild.servlets.json.serializers.BIMProjectSerializer;
import org.cmdbuild.servlets.utils.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BIM extends JSONBaseWithSpringContext {

	@JSONExported
	public JSONObject read( //
			final @Parameter(value = START) int start, //
			final @Parameter(value = LIMIT) int limit //
	) throws JSONException, CMDBException {
		final List<BIMProject> projects = bimLogic().read();
		final JSONArray jsonProjects = BIMProjectSerializer.toClient(projects);
		final JSONObject response = new JSONObject();

		response.put(BIM_PROJECTS, jsonProjects);

		return response;
	}
}
