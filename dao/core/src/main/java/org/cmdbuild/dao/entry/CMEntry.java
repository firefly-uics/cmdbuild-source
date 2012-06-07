package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

/**
 * Immutable data store entry
 */
public interface CMEntry extends CMValueSet {

	public interface CMEntryDefinition {
		public CMEntryDefinition set(final String key, final Object value);
		public CMEntry save();
	}

	public CMEntryType getType();

	public Object getId();

	public String getUser();
	public DateTime getBeginDate();
	public DateTime getEndDate();
}
