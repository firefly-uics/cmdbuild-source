package org.cmdbuild.servlets.json.serializers;

import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.defaultIfEmpty;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.json.JSONException;
import org.json.JSONObject;

public class LookupSerializer {

	public static JSONObject serializeLookup(final Lookup lookup) throws JSONException {
		return serializeLookup(lookup, false);
	}

	public static JSONObject serializeLookup(final Lookup lookup, final boolean shortForm) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("Id", lookup.getId());
			serializer.put("Description", lookup.description);

			if (!shortForm) {
				serializer.put("Type", lookup.type.name);
				serializer.put("Code", defaultIfEmpty(lookup.code, EMPTY));
				serializer.put("Number", lookup.number);
				serializer.put("Notes", lookup.notes);
				serializer.put("Default", lookup.isDefault);
				serializer.put("Active", lookup.active);
			}

			final Lookup parent = lookup.parent;
			if (parent != null) {
				serializer.put("ParentId", parent.getId());
				if (!shortForm) {
					serializer.put("ParentDescription", parent.description);
					serializer.put("ParentType", parent.type);
				}
			}
		}
		return serializer;
	}

	public static JSONObject serializeLookupParent(final Lookup lookup) throws JSONException {
		JSONObject serializer = null;
		if (lookup != null) {
			serializer = new JSONObject();
			serializer.put("ParentId", lookup.getId());
			serializer.put("ParentDescription", lookup.description);
		}
		return serializer;
	}

	public static JSONObject serializeLookupTable(final LookupType lookupType) throws JSONException {
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
