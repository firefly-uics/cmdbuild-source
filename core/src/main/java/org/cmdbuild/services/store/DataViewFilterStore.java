package org.cmdbuild.services.store;

import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.privileges.GrantCleaner;
import org.cmdbuild.services.localization.LocalizableStorableVisitor;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DataViewFilterStore implements FilterStore {

	private class FilterCard implements Filter {

		private final Filter filter;

		public FilterCard(final Filter filter) {
			this.filter = filter;
		}

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
		public String getValue() {
			return filter.getValue();
		}

		@Override
		public String getClassName() {
			return filter.getClassName();
		}

		@Override
		public String getPrivilegeId() {
			return String.format("Filter:%d", getId());
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == this) {
				return true;
			}
			if (!(obj instanceof Filter)) {
				return false;
			}
			final Filter filter = Filter.class.cast(obj);
			return this.getName().equals(filter.getName()) //
					&& this.getValue().equals(filter.getValue());
		}

		@Override
		public String toString() {
			return getValue();
		}

		@Override
		public boolean isTemplate() {
			return filter.isTemplate();
		}

		@Override
		public void accept(final LocalizableStorableVisitor visitor) {
			visitor.visit(this);
		}

		@Override
		public String getIdentifier() {
			throw new UnsupportedOperationException("should be never called");
		}

	}

	public static class DataViewGetFiltersResponse implements GetFiltersResponse {
		private final Iterable<Filter> filters;
		private final int totalSize;

		public DataViewGetFiltersResponse(final Iterable<Filter> filters, final int totalSize) {
			this.filters = filters;
			this.totalSize = totalSize;
		}

		@Override
		public Iterator<Filter> iterator() {
			return filters.iterator();
		}

		@Override
		public int count() {
			return totalSize;
		}

	}

	protected static final String FILTERS_CLASS_NAME = "_Filter";
	private static final String ID_ATTRIBUTE_NAME = "Id";
	private static final String MASTER_ATTRIBUTE_NAME = "IdOwner";
	protected static final String NAME_ATTRIBUTE_NAME = "Code";
	protected static final String DESCRIPTION_ATTRIBUTE_NAME = "Description";
	protected static final String FILTER_ATTRIBUTE_NAME = "Filter";
	protected static final String ENTRYTYPE_ATTRIBUTE_NAME = "IdSourceClass";
	protected static final String TEMPLATE_ATTRIBUTE_NAME = "Template";

	private final CMDataView view;
	private final OperationUser operationUser;
	private final GrantCleaner grantCleaner;
	private final StorableConverter<FilterStore.Filter> converter;

	public DataViewFilterStore(final CMDataView dataView, final OperationUser operationUser,
			final StorableConverter<FilterStore.Filter> converter) {
		this.view = dataView;
		this.operationUser = operationUser;
		this.grantCleaner = new GrantCleaner(view);
		this.converter = converter;
	}

	public CMClass getFilterClass() {
		return view.findClass(FILTERS_CLASS_NAME);
	}

	public Filter fetchFilter(final Long filterId) {
		final CMClass filterClass = getFilterClass();
		final String idAttributeName = ID_ATTRIBUTE_NAME;
		final CMQueryRow row = view //
				.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, idAttributeName), eq(filterId))) //
				.orderBy(filterClass.getCodeAttributeName(), //
						Direction.ASC) //
				.run().getOnlyRow();
		return new FilterCard(converter.convert(row.getCard(filterClass)));
	}

	@Override
	public GetFiltersResponse getAllUserFilters(final String className, final int offset, final int limit) {
		logger.info("getting all filters");
		final CMUser user = null;
		final CMQueryResult rawFilters = fetchUserFilters(className, user, offset, limit);
		final Iterable<Filter> filters = transform(rawFilters, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				final CMCard filterCard = input.getCard(getFilterClass());
				return new FilterCard(converter.convert(filterCard));
			}
		});

		return new DataViewGetFiltersResponse(filters, rawFilters.totalSize());
	}

	@Override
	public GetFiltersResponse getAllUserFilters() {
		logger.info("getting all filters");
		final CMUser user = null;
		final String entryTypeName = null;
		final CMQueryResult allUserFilters = fetchUserFilters(user, entryTypeName);

		final Iterable<Filter> filters = transform(allUserFilters, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				final CMCard filterCard = input.getCard(getFilterClass());
				return new FilterCard(converter.convert(filterCard));
			}
		});

		return new DataViewGetFiltersResponse(filters, allUserFilters.totalSize());
	}

	private CMQueryResult fetchUserFilters(final CMUser user, final String entryTypeName) {
		logger.info("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		return view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(onlyEntryTypeWithName(entryTypeName), //
						filtersAssociatedTo(user), isUserFilter())) //
				.orderBy(filterClass.getCodeAttributeName(), //
						Direction.ASC) //
				.count() //
				.run();
	}

	private CMQueryResult fetchUserFilters(final String className, final CMUser user, final int offset, final int limit) {
		logger.info("getting all filter cards");
		final CMClass clazz = view.findClass(className);
		final CMClass filterClass = getFilterClass();
		return view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(filtersAssociatedTo(user), isUserFilter(), matchTableId(clazz.getId()))) //
				.offset(offset) //
				.limit(limit) //
				.orderBy(filterClass.getCodeAttributeName(), Direction.ASC) //
				.count() //
				.run();
	}

	private WhereClause filtersAssociatedTo(final CMUser user) {
		WhereClause clause = trueWhereClause();

		if (user != null) {
			clause = condition( //
					attribute(getFilterClass(), MASTER_ATTRIBUTE_NAME), //
					eq(user.getId()) //
			);
		}

		return clause;
	}

	private WhereClause matchTableId(final Long tableId) {
		return condition(attribute(getFilterClass(), ENTRYTYPE_ATTRIBUTE_NAME), eq(tableId));
	}

	private WhereClause isUserFilter() {
		final WhereClause whereClause = condition(attribute(getFilterClass(), TEMPLATE_ATTRIBUTE_NAME), eq(false));
		return whereClause;
	}

	private WhereClause onlyEntryTypeWithName(final String entryTypeName) {
		WhereClause clause = trueWhereClause();

		if (entryTypeName != null) {
			final CMClass entryType = view.findClass(entryTypeName);
			if (entryType != null) {
				clause = condition( //
						attribute(getFilterClass(), ENTRYTYPE_ATTRIBUTE_NAME), //
						eq(entryType.getId()) //
				);
			}
		}

		return clause;
	}

	/**
	 * Retrieves all filters that the user can see (filters defined by itself
	 * and readable group filters)
	 */
	@Override
	public GetFiltersResponse getFiltersForCurrentlyLoggedUser(final String className) {
		logger.info("getting all filters");
		final CMUser user = operationUser.getAuthenticatedUser();
		final CMQueryResult result = fetchUserFilters(user, className);
		final Iterable<Filter> userFilters = transform(result, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				final CMCard filterCard = input.getCard(getFilterClass());
				return new FilterCard(converter.convert(filterCard));
			}
		});

		final Iterable<Filter> readableFiltersForCurrenlyLoggedUser = Iterables.concat( //
				userFilters, //
				fetchReadableGroupFiltersForCurrentlyLoggedUser(className) //
				);

		return new DataViewGetFiltersResponse(readableFiltersForCurrenlyLoggedUser, result.totalSize());
	}

	private Iterable<Filter> fetchReadableGroupFiltersForCurrentlyLoggedUser(final String className) {
		final Iterable<Filter> allGroupFilters = fetchAllGroupsFilters();
		final List<Filter> result = Lists.newArrayList();
		for (final Filter filter : allGroupFilters) {
			if (filter.getClassName().equals(className)
					&& (operationUser.hasAdministratorPrivileges() || operationUser.hasReadAccess(filter))) {
				result.add(filter);
			}
		}
		return result;
	}

	@Override
	public GetFiltersResponse fetchAllGroupsFilters() {
		logger.info("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final List<Filter> groupFilters = Lists.newArrayList();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, TEMPLATE_ATTRIBUTE_NAME), eq(true))) //
				.orderBy(filterClass.getCodeAttributeName(), //
						Direction.ASC) //
				.count() //
				.run();
		return convertResultsToFilterList(groupFilters, result);
	}

	private GetFiltersResponse convertResultsToFilterList(final List<Filter> groupFilters, final CMQueryResult result) {
		for (final CMQueryRow row : result) {
			final CMCard card = row.getCard(getFilterClass());
			final Filter filter = new FilterCard(converter.convert(card));
			if (operationUser.hasReadAccess(filter)) {
				groupFilters.add(filter);
			}
		}
		return new DataViewGetFiltersResponse(groupFilters, result.totalSize());
	}

	@Override
	public GetFiltersResponse fetchAllGroupsFilters(final int start, final int limit) {
		logger.info("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final List<Filter> groupFilters = Lists.newArrayList();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, TEMPLATE_ATTRIBUTE_NAME), eq(true))) //
				.offset(start).limit(limit).orderBy(filterClass.getCodeAttributeName(), //
						Direction.ASC) //
				.count() //
				.run();
		return convertResultsToFilterList(groupFilters, result);
	}

	@Override
	public Filter create(final Filter filter) {
		Validate.isTrue(isNotBlank(filter.getName()), "invalid filter name");
		Validate.notNull(filter.getClassName());
		final CMClass clazz = view.findClass(filter.getClassName());
		final CMCard.CMCardDefinition filterCardDefinition = view.createCardFor(getFilterClass()) //
				.set(MASTER_ATTRIBUTE_NAME, operationUser.getAuthenticatedUser().getId()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(TEMPLATE_ATTRIBUTE_NAME, filter.isTemplate()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId());
		final CMCard cardSaved = filterCardDefinition.save();
		return new FilterCard(converter.convert(cardSaved));
	}

	@Override
	public Filter update(final Filter filter) {
		final CMClass clazz = view.findClass(filter.getClassName());
		final CMCard card = getFilter(filter.getId());
		final CMCard.CMCardDefinition filterCardDefinition = view.update(card);
		filterCardDefinition.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId()); //
		final CMCard cardSaved = filterCardDefinition.save();
		return new FilterCard(converter.convert(cardSaved));
	}

	@Override
	public void delete(final Filter filter) {
		final CMCard filterCardToBeDeleted = getFilter(filter.getId());
		view.delete(filterCardToBeDeleted);
		grantCleaner.deleteGrantReferingTo(filter.getId());
	}

	@Override
	public Long getPosition(final Filter filter) {
		final CMClass filterClass = getFilterClass();
		final String idAttributeName = ID_ATTRIBUTE_NAME;
		final CMQueryRow row = view //
				.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.numbered(condition(attribute(filterClass, idAttributeName), eq(filter.getId()))) //
				.orderBy(filterClass.getCodeAttributeName(), //
						Direction.ASC) //
				.run().getOnlyRow();

		return row.getNumber();
	}

	private CMCard getFilter(final Long filterId) {
		logger.info("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final CMQueryRow row = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, ID_ATTRIBUTE_NAME), eq(Long.valueOf(filterId)))) //
				.run().getOnlyRow();

		return row.getCard(filterClass);
	}
}
