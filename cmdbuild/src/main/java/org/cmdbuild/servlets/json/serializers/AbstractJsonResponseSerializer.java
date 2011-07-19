package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entry.CMLookup;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.DateTimeAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.TimeAttributeType;
import org.cmdbuild.elements.Lookup;
import org.cmdbuild.operation.management.LookupOperation;
import org.cmdbuild.services.auth.UserContext;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class AbstractJsonResponseSerializer {

	@Deprecated // Needed because the new DAO does not fully support the lookups yet
	private LookupOperation systemLookupOperation = new LookupOperation(UserContext.systemContext());

	// TODO should be defined in the user session
	public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy HH:mm:ss");
	public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormat.forPattern("HH:mm:ss");
	public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormat.forPattern("dd/MM/yy");

	protected final String formatDateTime(final DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return DATE_TIME_FORMATTER.print(dateTime);
		}
	}

	// FIXME Horrible
	protected final Object javaToJsonValue(final CMAttributeType<?> type, final Object value) throws JSONException {
		if (value instanceof DateTime) {
			if (type instanceof DateTimeAttributeType) {
				return DATE_TIME_FORMATTER.print((DateTime) value);
			} else if (type instanceof TimeAttributeType) {
				return TIME_FORMATTER.print((DateTime) value);
			} else if (type instanceof DateAttributeType) {
				return DATE_FORMATTER.print((DateTime) value);
			}
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
