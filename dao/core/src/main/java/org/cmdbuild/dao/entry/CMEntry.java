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

		// TODO check if this is really needed
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

	/**
	 * Returns the value of the specified attribute.
	 * 
	 * @param key
	 *            the name of the attribute.
	 * @param requiredType
	 *            type the bean must match; can be an interface or superclass.
	 *            {@code null} is disallowed.
	 * 
	 * @return the value of the attribute.
	 * 
	 * @throws IllegalArgumentException
	 *             if attribute is not present.
	 */
	<T> T get(String key, Class<? extends T> requiredType);

	Iterable<Map.Entry<String, Object>> getValues();

	String getUser();

	DateTime getBeginDate();

	DateTime getEndDate();

}
