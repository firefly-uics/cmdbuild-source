package org.cmdbuild.logic.filter;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

public class DefaultFilterLogic implements FilterLogic {

	private static final Marker MARKER = MarkerFactory.getMarker(FilterLogic.class.getName());

	private final FilterStore store;

	public DefaultFilterLogic(final FilterStore store) {
		this.store = store;
	}

	@Override
	public Filter create(final Filter filter) {
		logger.info(MARKER, "creating filter '{}'", filter);
		final FilterStore.Filter created = store.create(FilterDTO.newFilter() //
				.withId(filter.getId()) //
				.withName(filter.getName()) //
				.withDescription(filter.getDescription()) //
				.withValue(filter.getConfiguration()) //
				.forClass(filter.getClassName()) //
				.asTemplate(filter.isTemplate()) //
				.build());
		return wrap(created);
	}

	@Override
	public void update(final Filter filter) {
		logger.info(MARKER, "updating filter '{}'", filter);
		store.update(FilterDTO.newFilter() //
				.withId(filter.getId()) //
				.withName(filter.getName()) //
				.withDescription(filter.getDescription()) //
				.withValue(filter.getConfiguration()) //
				.forClass(filter.getClassName()) //
				.build());
	}

	@Override
	public void delete(final Filter filter) {
		logger.info(MARKER, "deleting filter '{}'", filter);
		store.delete(FilterDTO.newFilter() //
				.withId(filter.getId()) //
				.build());
	}

	@Override
	public Long position(final Filter filter) {
		logger.info(MARKER, "getting position for filter '{}'", filter);
		return store.getPosition(FilterDTO.newFilter() //
				.withId(filter.getId()) //
				.build());
	}

	@Override
	public PagedElements<Filter> getFiltersForCurrentlyLoggedUser(final String className) {
		logger.info(MARKER, "getting all filters for class '{}' for the currently logged user", className);
		final FilterStore.GetFiltersResponse response = store.getFiltersForCurrentlyLoggedUser(className);
		return new PagedElements<FilterLogic.Filter>(wrap(response), response.count());
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters(final int start, final int limit) {
		logger.info(MARKER, "getting all filters starting from '{}' and with a limit of '{}'", start, limit);
		final FilterStore.GetFiltersResponse response = store.fetchAllGroupsFilters(start, limit);
		return new PagedElements<FilterLogic.Filter>(wrap(response), response.count());
	}

	@Override
	public PagedElements<Filter> getAllUserFilters(final String className, final int start, final int limit) {
		logger.info(MARKER, "getting all filters for class '{}' starting from '{}' and with a limit of '{}'",
				className, start, limit);
		final FilterStore.GetFiltersResponse response = store.getAllUserFilters(className, start, limit);
		return new PagedElements<FilterLogic.Filter>(wrap(response), response.count());
	}

	/**
	 * @deprecated do it better.
	 */
	@Deprecated
	private static FluentIterable<Filter> wrap(final FilterStore.GetFiltersResponse response) {
		return from(response) //
				.transform(new Function<FilterStore.Filter, Filter>() {

					@Override
					public Filter apply(final FilterStore.Filter input) {
						return wrap(input);
					}

				});
	}

	/**
	 * @deprecated do it better.
	 */
	@Deprecated
	private static Filter wrap(final FilterStore.Filter filter) {
		return new Filter() {

			@Override
			public Long getId() {
				return filter.getId();
			}

			@Override
			public String getName() {
				return filter.getName();
			}

			@Override
			public String getDescription() {
				return filter.getDescription();
			}

			@Override
			public String getClassName() {
				return filter.getClassName();
			}

			@Override
			public String getConfiguration() {
				return filter.getValue();
			}

			@Override
			public boolean isTemplate() {
				return filter.isTemplate();
			}

		};
	}

}
