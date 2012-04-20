package org.cmdbuild.dao.entry;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

/**
 * Immutable data store entry
 */
public interface CMEntry {

	interface CMEntryDefinition {
		CMEntryDefinition set(String key, Object value);
		CMEntry save();
	}

	CMEntryType getType();

	Object getId();
	Object get(String key);
	Iterable<Map.Entry<String, Object>> getValues();

	String getUser();
	DateTime getBeginDate();
	DateTime getEndDate();
}
