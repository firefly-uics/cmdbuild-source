package org.cmdbuild.services.store;

import org.cmdbuild.logger.Log;
import org.slf4j.Logger;

public interface FilterStore {

	Logger logger = Log.CMDBUILD;

	interface Filter {

		String getId();

		String getName();

		String getDescription();

		/**
		 * Mark the filter as a template
		 * for a group filter
		 * 
		 * @return
		 */
		boolean isTemplate();

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
	 * Support interface to have also the
	 * count of retrieved filters
	 */
	interface GetFiltersResponse extends Iterable<Filter>{
		int count();
	}

	/**
	 * 
	 * @return the filters for all the users
	 */
	// TODO only the administrator
	GetFiltersResponse getAllFilters(int start, int limit);

	/**
	 * 
	 * @return the filters for all the users
	 */
	// TODO only the administrator
	GetFiltersResponse getAllFilters();

	/**
	 * 
	 * @return the filters defined for the logged user for a given class
	 */
	GetFiltersResponse getUserFilters(String className);

	/**
	 * Saves a new filter in the database
	 * 
	 * @return the saved filter
	 */
	Filter create(Filter filter);

	/**
	 * Update an existent filter
	 * 
	 * @return
	 */
	Filter update(Filter filter);

	/**
	 * Deletes the filter from the database
	 * 
	 * @param filter
	 *            is the filter that will be deleted from database
	 */
	void delete(Filter filter);

	/**
	 * Retrieve the position of this filter
	 * This could be useful to calculate the page to
	 * have a given filter
	 * 
	 * @param filter
	 * the filter to looking for the position
	 * @return the position of this filter in the stored order
	 */
	Long getPosition(Filter filter);
}
