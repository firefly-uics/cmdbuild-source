package org.cmdbuild.servlets.json.serializers;

import static org.cmdbuild.common.Constants.DATETIME_FOUR_DIGIT_YEAR_FORMAT;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.DESCRIPTION;
import static org.cmdbuild.servlets.json.ComunicationConstants.ID;
import static org.cmdbuild.servlets.json.ComunicationConstants.LAST_CHECKIN;
import static org.cmdbuild.servlets.json.ComunicationConstants.NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.IMPORT_MAPPING;
import static org.cmdbuild.servlets.json.ComunicationConstants.EXPORT_MAPPING;
import static org.cmdbuild.servlets.json.ComunicationConstants.SYNCHRONIZED;

import java.util.List;

import org.cmdbuild.model.bim.BimProjectInfo;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BimProjectSerializer {

	public static JSONObject toClient(final BimProjectInfo bimProject) throws JSONException {
		final JSONObject out = new JSONObject();

		out.put(ID, bimProject.getProjectId());
		out.put(NAME, bimProject.getName());
		out.put(DESCRIPTION, bimProject.getDescription());
		out.put(ACTIVE, bimProject.isActive());
		out.put(SYNCHRONIZED, bimProject.isSynch());
		out.put(IMPORT_MAPPING, bimProject.getImportMapping());
		out.put(EXPORT_MAPPING, bimProject.getExportMapping());
		final DateTime lastCheckin = bimProject.getLastCheckin();
		
		if (lastCheckin != null) {
			final DateTimeFormatter formatter = DateTimeFormat.forPattern(DATETIME_FOUR_DIGIT_YEAR_FORMAT);
			out.put(LAST_CHECKIN, formatter.print(lastCheckin)); 
		}

		return out;
	}

	public static JSONArray toClient(final List<BimProjectInfo> bimProjects) throws JSONException {
		final JSONArray out = new JSONArray();

		for (final BimProjectInfo bimProject: bimProjects) {
			out.put(toClient(bimProject));
		}

		return out;
	}
}
