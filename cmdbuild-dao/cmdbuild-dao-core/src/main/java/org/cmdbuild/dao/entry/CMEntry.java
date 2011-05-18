package org.cmdbuild.dao.entry;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;

public interface CMEntry {

	public interface CMEntryDefinition {
		public CMEntryDefinition set(final String key, final Object value);
		public CMEntry save();
	}

	public CMEntryType getType();

	public Object getId();
	public Object get(final String key);
	public Iterable<Map.Entry<String, Object>> getValues();
}
