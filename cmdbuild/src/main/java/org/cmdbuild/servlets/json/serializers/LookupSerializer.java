package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.cmdbuild.data.store.lookup.LookupDto;
import org.cmdbuild.data.store.lookup.LookupTypeDto;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	public static JSONObject serializeLookup(final LookupDto lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public static JSONObject serializeLookup(final LookupDto lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("Id", lookup.id);
			serializer.put("Description", lookup.description);

			if (!shortForm) {
				serializer.put("Type", lookup.type.name);
				serializer.put("Code", defaultIfEmpty(lookup.code, EMPTY));
				serializer.put("Number", lookup.number);
				serializer.put("Notes", lookup.notes);
				serializer.put("Default", lookup.isDefault);
				serializer.put("Active", lookup.active);
			}

			final LookupDto parent = lookup.parent;
			if (parent != null) {
				serializer.put("ParentId", parent.id);
				if (!shortForm) {
					serializer.put("ParentDescription", parent.description);
					serializer.put("ParentType", parent.type);
				}
			}
		}
		return serializer;
	}

	public static JSONObject serializeLookupParent(final LookupDto lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("ParentId", lookup.id);
			serializer.put("ParentDescription", lookup.description);
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupTypeDto lookupType) throws JSONException {
		final JSONObject serializer = new JSONObject();
		serializer.put("id", lookupType.name);
		serializer.put("text", lookupType.name);
		serializer.put("type", "lookuptype");
		serializer.put("selectable", true);

		if (lookupType.parent != null) {
			serializer.put("parent", lookupType.parent);
		}
		return serializer;
	}

}
