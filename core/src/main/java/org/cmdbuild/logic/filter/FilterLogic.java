package org.cmdbuild.logic.filter;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.Logic;

public interface FilterLogic extends Logic {

	interface Filter {

		Long getId();

		String getName();

		String getDescription();

		String getClassName();

		String getConfiguration();

		boolean isShared();

	}

	Filter create(Filter filter);

	void update(Filter filter);

	void delete(Filter filter);

	Long position(Filter filter);

	PagedElements<Filter> getFiltersForCurrentUser(String className);

	PagedElements<Filter> fetchAllGroupsFilters(int start, int limit);

	PagedElements<Filter> getAllUserFilters(String className, int start, int limit);

}
