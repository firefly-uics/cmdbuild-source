package org.cmdbuild.servlets.json;

import static org.cmdbuild.servlets.json.ComunicationConstants.CLASS_NAME;
import static org.cmdbuild.servlets.json.ComunicationConstants.ACTIVE;
import static org.cmdbuild.servlets.json.ComunicationConstants.BIM_ROOT;
import static org.cmdbuild.servlets.json.ComunicationConstants.EXPORT;

import java.util.List;

import org.cmdbuild.model.bim.BimLayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BimLayerSerializer {

	public static JSONArray toClient(final List<BimLayer> layerList)
			throws JSONException {
		final JSONArray out = new JSONArray();

		for (final BimLayer layer : layerList) {
			out.put(toClient(layer));
		}
		return out;
	}

	public static JSONObject toClient(final BimLayer layer)
			throws JSONException {
		final JSONObject out = new JSONObject();

		out.put(CLASS_NAME, layer.getClassName());
		out.put(ACTIVE, layer.isActive());
		out.put(BIM_ROOT, layer.isRoot());
		out.put(EXPORT, layer.isExport());
		return out;
	}

}
