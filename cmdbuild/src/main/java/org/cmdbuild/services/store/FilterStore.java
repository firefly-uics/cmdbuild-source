package org.cmdbuild.services.store;

public interface FilterStore extends Store {

	interface Filter {

		String getName();

		String getDescription();

		String getValue();

		Long getEntryTypeId();

	}

	Iterable<Filter> getAllFilters();

	void save(Filter filter);

}
