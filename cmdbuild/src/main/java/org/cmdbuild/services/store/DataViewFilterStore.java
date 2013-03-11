package org.cmdbuild.services.store;

import static com.google.common.collect.Iterables.transform;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Iterator;

import org.apache.commons.lang.Validate;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.reference.EntryTypeReference;
import org.cmdbuild.dao.view.CMDataView;

import com.google.common.base.Function;

public class DataViewFilterStore implements FilterStore {

	private static final String MASTER_ATTRIBUTE_NAME = "Master";
	private static final String NAME_ATTRIBUTE_NAME = "Code";
	private static final String DESCRIPTION_ATTRIBUTE_NAME = "Description";
	private static final String FILTER_ATTRIBUTE_NAME = "Filter";
	private static final String ENTRYTYPE_ATTRIBUTE_NAME = "TableId";

	private class FilterCard implements Filter {

		private final CMCard card;

		public String getId() {
			return card.getId().toString();
		}

		public FilterCard(final CMCard card) {
			this.card = card;
		}

		@Override
		public String getName() {
			return (String) card.get(NAME_ATTRIBUTE_NAME);
		}

		@Override
		public String getDescription() {
			return (String) card.get(DESCRIPTION_ATTRIBUTE_NAME);
		}

		@Override
		public String getValue() {
			return (String) card.get(FILTER_ATTRIBUTE_NAME);
		}

		@Override
		public String getClassName() {
			final EntryTypeReference etr = (EntryTypeReference) card.get(ENTRYTYPE_ATTRIBUTE_NAME);
			final CMClass clazz = dataView.findClass(etr.getId());
			return clazz.getName();
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
	}

	private class DataViewGetFiltersResponse implements GetFiltersResponse {
		private final Iterable<Filter> filters;
		private final int count;

		public DataViewGetFiltersResponse(Iterable<Filter> filters, int count) {
			this.filters = filters;
			this.count = count;
		}

		@Override
		public Iterator<Filter> iterator() {
			return filters.iterator();
		}

		@Override
		public int count() {
			return count;
		}
	}

	private static final String CLASS_NAME = "_Filters";

	private final CMDataView dataView;
	private final CMClass filterClass;
	private final OperationUser operationUser;

	public DataViewFilterStore(final CMDataView dataView, final OperationUser operationUser) {
		this.dataView = dataView;
		this.filterClass = dataView.findClass(CLASS_NAME);
		this.operationUser = operationUser;
	}

	public CMClass getFilterClass() {
		return filterClass;
	}

	@Override
	public GetFiltersResponse getAllFilters(final int offset, final int limit) {
		logger.info("getting all filters");
		final CMUser user = null;
		final CMQueryResult rawFilters = fetchFilters(user, offset, limit);
		Iterable<Filter> filters = transform(rawFilters, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				CMCard filterCard = input.getCard(filterClass);
				return new FilterCard(filterCard);
			}
		});

		return new DataViewGetFiltersResponse(filters, rawFilters.totalSize());
	}

	@Override
	public GetFiltersResponse getAllFilters() {
		logger.info("getting all filters");
		final CMUser user = null;
		final String entryTypeName = null;
		final CMQueryResult rawFilters = fetchFilters(user, entryTypeName);

		Iterable<Filter> filters = transform(rawFilters, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				CMCard filterCard = input.getCard(filterClass);
				return new FilterCard(filterCard);
			}
		});

		return new DataViewGetFiltersResponse(filters, rawFilters.totalSize());
	}

	@Override
	public GetFiltersResponse getUserFilters(String className) {
		logger.info("getting all filters");
		final CMUser user = operationUser.getAuthenticatedUser();
		final CMQueryResult rawFilters = fetchFilters(user, className);
		Iterable<Filter> filters = transform(rawFilters, new Function<CMQueryRow, Filter>() {
			@Override
			public Filter apply(final CMQueryRow input) {
				CMCard filterCard = input.getCard(filterClass);
				return new FilterCard(filterCard);
			}
		});

		return new DataViewGetFiltersResponse(filters, rawFilters.totalSize());
	}

	@Override
	public Filter save(final Filter filter) {
		Validate.isTrue(isNotBlank(filter.getName()), "invalid filter name");
		Validate.notNull(filter.getClassName());
		final CMClass clazz = dataView.findClass(filter.getClassName());
		final CMCard.CMCardDefinition filterCardDefinition = createOrModifyCard(filter) //
			.set(MASTER_ATTRIBUTE_NAME, operationUser.getAuthenticatedUser().getId()) //
			.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
			.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
			.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
			.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId());

		return new FilterCard(filterCardDefinition.save());
	}

	@Override
	public void delete(final Filter filter) {
		final CMCard filterCardToBeDeleted = getFilterCard(filter);
		dataView.delete(filterCardToBeDeleted);
	}

	@Override
	public Long getPosition(Filter filter) {
		String idAttributeName = "Id";
		final CMQueryRow row = dataView //
				.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.numbered(condition(attribute(filterClass, idAttributeName), eq(filter.getId()))) //
				.orderBy(filterClass.getCodeAttributeName(), //
					Direction.ASC) //
				.run().getOnlyRow();

		return row.getNumber();
	}

	private CMCard.CMCardDefinition createOrModifyCard(final Filter filter) {
		final CMCard card = getFilterCard(filter);
		final CMCard.CMCardDefinition def;
		if (card == null) {
			def = dataView.createCardFor(filterClass);
		} else {
			def = dataView.update(card);
		}
		return def;
	}

	private CMCard getFilterCard(final Filter filter) {
		final Iterator<CMCard> itr = getFilter(filter.getName(), filter.getClassName()).iterator();
		return itr.hasNext() ? itr.next() : null;
	}

	private CMQueryResult fetchFilters(final CMUser user, final String entryTypeName) {
		logger.info("getting all filter cards");
		return dataView.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(onlyEntryTypeWithName(entryTypeName), //
					filtersAssociatedToCurrentlyLoggedUserCondition(user))) //
				.orderBy(filterClass.getCodeAttributeName(), //
					Direction.ASC) //
				.run();
	}

	private CMQueryResult fetchFilters(final CMUser user, final int offset, final int limit) {
		logger.info("getting all filter cards");
		return dataView.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(filtersAssociatedToCurrentlyLoggedUserCondition(user)) //
				.offset(offset) //
				.limit(limit) //
				.orderBy(filterClass.getCodeAttributeName(), //
					Direction.ASC) //
				.run();
	}

	private WhereClause filtersAssociatedToCurrentlyLoggedUserCondition(final CMUser user) {
		WhereClause clause = new TrueWhereClause();

		if (user != null) {
			clause = condition( //
					attribute(filterClass, MASTER_ATTRIBUTE_NAME), //
					eq(user.getId()) //
				);
		}

		return clause;
	}

	private WhereClause onlyEntryTypeWithName(String entryTypeName) {
		WhereClause clause = new TrueWhereClause();

		if (entryTypeName != null) {
			CMClass entryType = dataView.findClass(entryTypeName);
			if (entryType != null) {
				clause = condition( //
						attribute(filterClass, ENTRYTYPE_ATTRIBUTE_NAME), //
						eq(entryType.getId()) //
					);
			}
		}

		return clause;
	}

	/**
	 * Note that now are returned only filters associated to the currently
	 * logged user
	 */
	private Iterable<CMCard> getFilter(final String filterName, final String className) {
		logger.info("getting all filter cards");
		final CMClass clazz = dataView.findClass(className);
		final CMQueryResult result = dataView.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(whereClauseFor(filterName, clazz.getId())) //
				.run();
		return transform(result, new Function<CMQueryRow, CMCard>() {
			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(filterClass);
			}
		});
	}

	private WhereClause whereClauseFor(final String filterName, final Long entryTypeId) {
		final CMUser user = operationUser.getAuthenticatedUser();
		final WhereClause userWhereClause = filtersAssociatedToCurrentlyLoggedUserCondition(user);
		final WhereClause entryTypeWhereClause = condition(attribute(filterClass, ENTRYTYPE_ATTRIBUTE_NAME),
				eq(entryTypeId));
		final WhereClause whereClause;
		if (isNotBlank(filterName)) {
			whereClause = and(userWhereClause, entryTypeWhereClause,
					condition(attribute(filterClass, NAME_ATTRIBUTE_NAME), eq(filterName)));
		} else {
			whereClause = userWhereClause;
		}
		return whereClause;
	}
}
