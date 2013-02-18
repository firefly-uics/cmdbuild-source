package org.cmdbuild.services.store;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface FilterStore {

	Logger logger = Log.CMDBUILD;

	interface Filter {

		String getName();

		String getDescription();

		/**
		 * It is the filter that contains rules for filtering the cards
		 */
		String getValue();

		/**
		 * 
		 * @return the name of the class to which the filter is associated.
		 */
		String getClassName();

	}

	/**
	 * 
	 * @return all the filters for the currently logged user
	 */
	Iterable<Filter> getAllFilters();

	/**
	 * Saves a new filter in the database or automatically updates it if exists
	 * another filter with the same name AND for the same entry type
	 */
	void save(Filter filter);

	/**
	 * Deletes the filter from the database
	 * 
	 * @param filter
	 *            is the filter that will be deleted from database
	 */
	void delete(Filter filter);

}
