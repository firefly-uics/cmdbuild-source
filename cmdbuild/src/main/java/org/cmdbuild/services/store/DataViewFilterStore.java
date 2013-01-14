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
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.where.WhereClause;
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
			final CMClass clazz = dataView.findClassById((Long) card.get(FILTER_ATTRIBUTE_NAME));
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

	private static final String CLASS_NAME = "_Filters";

	private final CMDataView dataView;
	private final CMClass filterClass;
	private final OperationUser operationUser;

	public DataViewFilterStore(final CMDataView dataView, final OperationUser operationUser) {
		this.dataView = dataView;
		this.filterClass = dataView.findClassByName(CLASS_NAME);
		this.operationUser = operationUser;
	}

	public CMClass getFilterClass() {
		return filterClass;
	}

	@Override
	public Iterable<Filter> getAllFilters() {
		logger.info("getting all filters");
		return transform(getAllFilterCards(), new Function<CMCard, Filter>() {
			@Override
			public Filter apply(final CMCard input) {
				return new FilterCard(input);
			}
		});
	}

	@Override
	public void save(final Filter filter) {
		Validate.isTrue(isNotBlank(filter.getName()), "invalid filter name");
		Validate.notNull(filter.getClassName());
		final CMClass clazz = dataView.findClassByName(filter.getClassName());
		createOrModifyCard(filter) //
				.set(MASTER_ATTRIBUTE_NAME, operationUser.getAuthenticatedUser().getId()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId()) //
				.save();
	}

	private CMCard.CMCardDefinition createOrModifyCard(final Filter filter) {
		final CMCard card = getFilterCard(filter);
		final CMCard.CMCardDefinition def;
		if (card == null) {
			def = dataView.newCard(filterClass);
		} else {
			def = dataView.modifyCard(card);
		}
		return def;
	}

	private CMCard getFilterCard(final Filter filter) {
		final Iterator<CMCard> itr = getFilters(filter.getName(), filter.getClassName()).iterator();
		return itr.hasNext() ? itr.next() : null;
	}

	private Iterable<CMCard> getAllFilterCards() {
		logger.info("getting all filter cards");
		final CMQueryResult result = dataView.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(filtersAssociatedToCurrentlyLoggedUser()) //
				.run();
		return transform(result, new Function<CMQueryRow, CMCard>() {
			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(filterClass);
			}
		});
	}

	private WhereClause filtersAssociatedToCurrentlyLoggedUser() {
		return condition(attribute(filterClass, MASTER_ATTRIBUTE_NAME),
				eq(operationUser.getAuthenticatedUser().getId()));
	}

	/**
	 * Note that now are returned only filters associated to the currently
	 * logged user
	 */
	private Iterable<CMCard> getFilters(final String filterName, final String className) {
		logger.info("getting all filter cards");
		final CMClass clazz = dataView.findClassByName(className);
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
		final WhereClause userWhereClause = filtersAssociatedToCurrentlyLoggedUser();
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
