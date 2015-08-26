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
		String getConfiguration();

		/**
		 * @return {@code true} if the filter is shared between all users,
		 *         {@code false} otherwise.
		 */
		boolean isShared();

		Long getUserId();

	}

	/**
	 * Gets the all user filters.
	 * 
	 * @param className
	 *            the name of the class, {@code null} means all classes.
	 * @param userId
	 *            the id of the user, {@code null} means all users.
	 */
	PagedElements<Filter> getAllUserFilters(String className, Long userId, int start, int limit);

	/**
	 * Gets the all group filters.
	 * 
	 * @param className
	 *            the name of the class, {@code null} means all classes.
	 */
	PagedElements<Filter> fetchAllGroupsFilters(String className, int start, int limit);

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
	void update(Filter filter);

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

	Iterable<Filter> getAllFilters(String className, String groupName);

	void join(String groupName, Iterable<Filter> filters);

	void disjoin(String groupName, Iterable<Filter> filters);

}