package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonResponseSerializer {

	@Deprecated // FIXME should be defined in the user session
	private LookupOperation systemLookupOperation = new LookupOperation(UserContext.systemContext());

	@Deprecated // FIXME should be defined in the user session
	private DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy HH:mm:ss");

	protected final String formatDate(final DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return DATE_TIME_FORMATTER.print(dateTime);
		}
	}

	protected final Object javaToJsonValue(final Object value) throws JSONException {
		if (value instanceof DateTime) {
			return formatDate((DateTime) value);
		} else if (value instanceof CMLookup) {
			final CMLookup lookup = (CMLookup) value;
			final Lookup oldLookup = systemLookupOperation.getLookupById((Integer) lookup.getId());
			return idAndDescription(oldLookup.getId(), oldLookup.getDescription());
		}
		return value;
	}

	private JSONObject idAndDescription(final Object id, final String description) throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		jsonObject.put("id", id);
		jsonObject.put("dsc", description);
		return jsonObject;
	}
}
