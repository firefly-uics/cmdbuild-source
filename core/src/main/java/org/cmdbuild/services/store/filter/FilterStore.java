package org.cmdbuild.services.store.filter;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.services.localization.LocalizableStorable;
import org.slf4j.Logger;

public interface FilterStore {

	Logger logger = Store.logger;

	interface Filter extends SerializablePrivilege, LocalizableStorable {

		@Override
		Long getId();

		@Override
		String getName();

		@Override
		String getDescription();

		/**
		 * @return the name of the class to which the filter is associated.
		 */
		String getClassName();

		/**
		 * @return the filter that contains rules for filtering the cards.
		 */
		String getValue();

		/**
		 * @return {@code true} if the filter is a template for a group filter,
		 *         {@code false} otherwise.
		 */
		boolean isTemplate();

	}

	/**
	 * 
	 * @return the filters for all the users
	 */
	// TODO only the administrator
	PagedElements<Filter> getAllUserFilters(String className, int start, int limit);

	/**
	 * 
	 * @return the filters for all the users
	 */
	// TODO only the administrator
	PagedElements<Filter> getAllUserFilters();

	/**
	 * 
	 * @return filters for all groups (i.e. filters marked as template in the
	 *         database)
	 */
	PagedElements<Filter> fetchAllGroupsFilters();

	PagedElements<Filter> fetchAllGroupsFilters(int start, int limit);

	/**
	 * 
	 * @return the filters defined for the logged user for a given class and
	 *         group filters that user can see
	 */
	PagedElements<Filter> getFiltersForCurrentlyLoggedUser(String className);

	/**
	 * Saves a new filter in the database
	 * 
	 * @return the saved filter
	 */
	Long create(Filter filter);

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
	 * Retrieve the position of this filter This could be useful to calculate
	 * the page to have a given filter
	 * 
	 * @param filter
	 *            the filter to looking for the position
	 * @return the position of this filter in the stored order
	 */
	Long getPosition(Filter filter);

	Filter fetchFilter(Long filterId);

}
