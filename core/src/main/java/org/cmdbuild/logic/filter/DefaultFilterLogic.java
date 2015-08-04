package org.cmdbuild.logic.filter;

import static com.google.common.collect.FluentIterable.from;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.services.store.FilterDTO;
import org.cmdbuild.services.store.FilterStore;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;

public class DefaultFilterLogic implements FilterLogic {

	public static interface Converter {

		FilterStore.Filter logicToStore(Filter filter);

		Filter storeToLogic(FilterStore.Filter filter);

	}

	public static class DefaultConverter implements Converter {

		private final com.google.common.base.Converter<Filter, FilterStore.Filter> delegate;

		public DefaultConverter(final com.google.common.base.Converter<Filter, FilterStore.Filter> delegate) {
			this.delegate = delegate;
		}

		@Override
		public FilterStore.Filter logicToStore(final Filter filter) {
			return delegate.convert(filter);
		}

		@Override
		public Filter storeToLogic(final FilterStore.Filter filter) {
			return delegate.reverse().convert(filter);
		}

	}

	public static class FilterConverter extends com.google.common.base.Converter<Filter, FilterStore.Filter> {

		@Override
		protected FilterStore.Filter doForward(final Filter a) {
			return FilterDTO.newFilter() //
					.withId(a.getId()) //
					.withName(a.getName()) //
					.withDescription(a.getDescription()) //
					.withValue(a.getConfiguration()) //
					.forClass(a.getClassName()) //
					.asTemplate(a.isTemplate()) //
					.build();
		}

		@Override
		protected Filter doBackward(final FilterStore.Filter b) {
			// TODO do it better
			return new Filter() {

				@Override
				public Long getId() {
					return b.getId();
				}

				@Override
				public String getName() {
					return b.getName();
				}

				@Override
				public String getDescription() {
					return b.getDescription();
				}

				@Override
				public String getClassName() {
					return b.getClassName();
				}

				@Override
				public String getConfiguration() {
					return b.getValue();
				}

				@Override
				public boolean isTemplate() {
					return b.isTemplate();
				}

			};
		}
	}

	private static final Marker MARKER = MarkerFactory.getMarker(FilterLogic.class.getName());

	private final FilterStore store;
	private final Converter converter;

	public DefaultFilterLogic(final FilterStore store, final Converter converter) {
		this.store = store;
		this.converter = converter;
	}

	@Override
	public Filter create(final Filter filter) {
		logger.info(MARKER, "creating filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		final FilterStore.Filter created = store.create(_filter);
		return converter.storeToLogic(created);
	}

	@Override
	public void update(final Filter filter) {
		logger.info(MARKER, "updating filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		store.update(_filter);
	}

	@Override
	public void delete(final Filter filter) {
		logger.info(MARKER, "deleting filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		store.delete(_filter);
	}

	@Override
	public Long position(final Filter filter) {
		logger.info(MARKER, "getting position for filter '{}'", filter);
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		return store.getPosition(_filter);
	}

	@Override
	public PagedElements<Filter> getFiltersForCurrentlyLoggedUser(final String className) {
		logger.info(MARKER, "getting all filters for class '{}' for the currently logged user", className);
		final FilterStore.GetFiltersResponse response = store.getFiltersForCurrentlyLoggedUser(className);
		return new PagedElements<FilterLogic.Filter>(from(response) //
				.transform(toLogic()), //
				response.count());
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters(final int start, final int limit) {
		logger.info(MARKER, "getting all filters starting from '{}' and with a limit of '{}'", start, limit);
		final FilterStore.GetFiltersResponse response = store.fetchAllGroupsFilters(start, limit);
		return new PagedElements<FilterLogic.Filter>(from(response) //
				.transform(toLogic()), //
				response.count());
	}

	@Override
	public PagedElements<Filter> getAllUserFilters(final String className, final int start, final int limit) {
		logger.info(MARKER, "getting all filters for class '{}' starting from '{}' and with a limit of '{}'",
				className, start, limit);
		final FilterStore.GetFiltersResponse response = store.getAllUserFilters(className, start, limit);
		return new PagedElements<FilterLogic.Filter>(from(response) //
				.transform(toLogic()), //
				response.count());
	}

	private Function<FilterStore.Filter, Filter> toLogic() {
		return new Function<FilterStore.Filter, Filter>() {

			@Override
			public Filter apply(final FilterStore.Filter input) {
				return converter.storeToLogic(input);
			}

		};
	}

}
