package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

/**
 * Immutable data store entry
 */
public interface CMEntry extends CMValueSet {

	interface CMEntryDefinition {
		CMEntryDefinition set(String key, Object value);
		CMEntry save();
	}

	CMEntryType getType();

	Object getId();

	String getUser();
	DateTime getBeginDate();
	DateTime getEndDate();
}
