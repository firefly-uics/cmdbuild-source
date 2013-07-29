package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.common.Constants.DATETIME_FOUR_DIGIT_YEAR_FORMAT;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LAST_CHECKIN;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;

import java.util.List;

import org.cmdbuild.model.bim.BIMProject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BIMProjectSerializer {

	public static JSONObject toClient(final BIMProject bimProject) throws JSONException {
		final JSONObject out = new JSONObject();

		out.put(NAME, bimProject.getName());
		out.put(ID, bimProject.getProjectId());
		out.put(DESCRIPTION, bimProject.getDescription());
		out.put(ACTIVE, bimProject.isActive());
		final DateTime lastCheckin = bimProject.getLastCheckin();
		
		if (lastCheckin != null) {
			final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATETIME_FOUR_DIGIT_YEAR_FORMAT);
			out.put(LAST_CHECKIN, formatter.print(lastCheckin)); 
		}

		return out;
	}

	public static JSONArray toClient(final List<BIMProject> bimProjects) throws JSONException {
		final JSONArray out = new JSONArray();

		for (final BIMProject bimProject: bimProjects) {
			out.put(toClient(bimProject));
		}

		return out;
	}
}
