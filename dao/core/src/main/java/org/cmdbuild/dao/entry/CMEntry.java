package org.cmdbuild.dao.entry;

import java.util.Map;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.joda.time.DateTime;

/**
 * Immutable data store entry.
 */
public interface CMEntry {

	/**
	 * {@link CMEntry} mutator object.
	 */
	interface CMEntryDefinition {

		CMEntryDefinition set(String key, Object value);

		CMEntry save();

	}

	CMEntryType getType();

	Long getId();

	/**
	 * Returns the value of the specified attribute.
	 * 
	 * @param key
	 *            the name of the attribute.
	 * 
	 * @return the value of the attribute.
	 * 
	 * @throws IllegalArgumentException
	 *             if attribute is not present.
	 */
	Object get(String key);

	Iterable<Map.Entry<String, Object>> getValues();

	String getUser();

	DateTime getBeginDate();

	DateTime getEndDate();

}
