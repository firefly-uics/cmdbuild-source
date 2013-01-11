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
	private static final String ENTRYTYPE_ATTRIBUTE_NAME = "IdClass";

	private static class FilterCard implements Filter {

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
	private final CMClass clazz;
	private final OperationUser operationUser;

	public DataViewFilterStore(final CMDataView dataView, final OperationUser operationUser) {
		this.dataView = dataView;
		this.clazz = dataView.findClassByName(CLASS_NAME);
		this.operationUser = operationUser;
	}

	public CMClass getFilterClass() {
		return clazz;
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
		createOrModifyCard(filter) //
				.set(MASTER_ATTRIBUTE_NAME, operationUser.getAuthenticatedUser().getId()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.save();
	}

	private CMCard.CMCardDefinition createOrModifyCard(final Filter filter) {
		final CMCard card = getFilterCard(filter);
		final CMCard.CMCardDefinition def;
		if (card == null) {
			def = dataView.newCard(clazz);
		} else {
			def = dataView.modifyCard(card);
		}
		return def;
	}

	private CMCard getFilterCard(final Filter filter) {
		final Iterator<CMCard> itr = getAllFilterCards(filter.getName()).iterator();
		return itr.hasNext() ? itr.next() : null;
	}

	private Iterable<CMCard> getAllFilterCards() {
		return getAllFilterCards(null);
	}

	private Iterable<CMCard> getAllFilterCards(final String name) {
		logger.info("getting all filter cards");
		final CMQueryResult result = dataView.select(anyAttribute(clazz)) //
				.from(clazz) //
				.where(whereClauseFor(name)) //
				.run();
		return transform(result, new Function<CMQueryRow, CMCard>() {
			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(clazz);
			}
		});
	}

	private WhereClause whereClauseFor(final String filterName) {
		final WhereClause userWhereClause = condition(attribute(clazz, MASTER_ATTRIBUTE_NAME), eq(operationUser
				.getAuthenticatedUser().getId()));
		final WhereClause whereClause;
		if (isNotBlank(filterName)) {
			whereClause = and(userWhereClause, condition(attribute(clazz, NAME_ATTRIBUTE_NAME), eq(filterName)));
		} else {
			whereClause = userWhereClause;
		}
		return whereClause;
	}

}
