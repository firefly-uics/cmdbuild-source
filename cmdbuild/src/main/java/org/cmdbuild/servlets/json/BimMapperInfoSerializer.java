package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_ROOT;

import java.util.List;

import org.cmdbuild.model.bim.BimMapperInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class BimMapperInfoSerializer {

	public static JSONArray toClient(final List<BimMapperInfo> bimMapperInfoList)  throws JSONException {
		final JSONArray out = new JSONArray();

		for (final BimMapperInfo bimMapperInfo : bimMapperInfoList) {
			out.put(toClient(bimMapperInfo));
		}
		return out;
	}

	public static JSONObject toClient(final BimMapperInfo bimMapperInfo)  throws JSONException {
		final JSONObject out = new JSONObject();

		out.put(CLASS_NAME, bimMapperInfo.getClassName());
		out.put(ACTIVE, bimMapperInfo.isActive());
		out.put(BIM_ROOT, bimMapperInfo.isBimRoot());
		return out;
	}

	
}
