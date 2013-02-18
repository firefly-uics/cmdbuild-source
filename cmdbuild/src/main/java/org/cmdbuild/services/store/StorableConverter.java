package org.cmdbuild.services.store;

import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.services.store.Store.Storable;

public interface StorableConverter<T extends Storable> {

	/**
	 * 
	 * @return the name of the class in the store.
	 */
	String getClassName();

	T convert(CMCard card);

	/**
	 * Converts a generic type into a map of <String, Object>, corresponding to
	 * attribute <name, value>
	 * 
	 * @param storable
	 * @return
	 */
	Map<String, Object> getValues(T storable);

}
