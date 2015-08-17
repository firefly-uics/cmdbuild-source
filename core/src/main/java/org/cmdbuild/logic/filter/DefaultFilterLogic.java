package org.cmdbuild.logic.filter;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static java.lang.Integer.MAX_VALUE;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.UserStore;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.services.store.filter.FilterDTO;
import org.cmdbuild.services.store.filter.FilterStore;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

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

		private final UserStore userStore;

		public FilterConverter(final UserStore userStore) {
			this.userStore = userStore;
		}

		@Override
		protected FilterStore.Filter doForward(final Filter a) {
			return FilterDTO.newFilter() //
					.withId(a.getId()) //
					.withName(a.getName()) //
					.withDescription(a.getDescription()) //
					.withValue(a.getConfiguration()) //
					.forClass(a.getClassName()) //
					.asTemplate(a.isTemplate()) //
					.withOwner(userStore.getUser().getAuthenticatedUser().getId()) //
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
	private final UserStore userStore;

	public DefaultFilterLogic(final FilterStore store, final Converter converter, final UserStore userStore) {
		this.store = store;
		this.converter = converter;
		this.userStore = userStore;
	}

	@Override
	public Filter create(final Filter filter) {
		logger.info(MARKER, "creating filter '{}'", filter);
		Validate.notBlank(filter.getName(), "missing name");
		final FilterStore.Filter _filter = converter.logicToStore(filter);
		final Long createdId = store.create(_filter);
		final FilterStore.Filter created = store.fetchFilter(createdId);
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
	public PagedElements<Filter> getFiltersForCurrentUser(final String className) {
		logger.info(MARKER, "getting all filters for class '{}' for the currently logged user", className);
		final OperationUser operationUser = userStore.getUser();
		final CMUser user = operationUser.getAuthenticatedUser();
		final PagedElements<FilterStore.Filter> userFilters = store.getAllUserFilters(className, user.getId(), 0,
				MAX_VALUE);
		final PagedElements<org.cmdbuild.services.store.filter.FilterStore.Filter> fetchAllGroupsFilters = store
				.fetchAllGroupsFilters(className, 0, MAX_VALUE);
		final Iterable<FilterStore.Filter> groupFilters = from(fetchAllGroupsFilters) //
				.filter(new Predicate<FilterStore.Filter>() {

					@Override
					public boolean apply(final FilterStore.Filter input) {
						return (operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(input));
					}

				});
		final Iterable<FilterStore.Filter> allFilters = concat(userFilters, groupFilters);
		return new PagedElements<Filter>( //
				from(allFilters) //
						.transform(toLogic()), //
				0);
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters(final int start, final int limit) {
		logger.info(MARKER, "getting all filters starting from '{}' and with a limit of '{}'", start, limit);
		final PagedElements<FilterStore.Filter> response = store.fetchAllGroupsFilters(null, start, limit);
		return new PagedElements<Filter>(from(response) //
				.transform(toLogic()), //
				response.totalSize());
	}

	@Override
	public PagedElements<Filter> getAllUserFilters(final String className, final int start, final int limit) {
		logger.info(MARKER, "getting all filters for class '{}' starting from '{}' and with a limit of '{}'",
				className, start, limit);
		final PagedElements<FilterStore.Filter> response = store.getAllUserFilters(className, null, start, limit);
		return new PagedElements<Filter>(from(response) //
				.transform(toLogic()), //
				response.totalSize());
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
