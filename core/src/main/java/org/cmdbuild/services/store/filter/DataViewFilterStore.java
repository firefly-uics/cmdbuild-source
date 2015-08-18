package org.cmdbuild.services.store.filter;

import static com.google.common.collect.FluentIterable.from;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.ASC;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.OperatorAndValues.eq;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.alwaysTrue;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.and;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.condition;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.not;
import static org.cmdbuild.services.store.filter.FilterConverter.CLASS_NAME;
import static org.cmdbuild.services.store.filter.FilterConverter.ENTRYTYPE;
import static org.cmdbuild.services.store.filter.FilterConverter.ID;
import static org.cmdbuild.services.store.filter.FilterConverter.NAME;
import static org.cmdbuild.services.store.filter.FilterConverter.OWNER;
import static org.cmdbuild.services.store.filter.FilterConverter.SHARED;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.privileges.GrantCleaner;

import com.google.common.base.Function;

public class DataViewFilterStore implements FilterStore {

	private final CMDataView view;
	private final GrantCleaner grantCleaner;
	private final StorableConverter<FilterStore.Filter> converter;

	public DataViewFilterStore(final CMDataView dataView, final StorableConverter<FilterStore.Filter> converter) {
		this.view = dataView;
		this.grantCleaner = new GrantCleaner(view);
		this.converter = converter;
	}

	public CMClass getFilterClass() {
		return view.findClass(CLASS_NAME);
	}

	@Override
	public Filter fetchFilter(final Long filterId) {
		return cardToFilter().apply(card(filterId));
	}

	@Override
	public PagedElements<Filter> getAllUserFilters(final String className, final Long userId, final int offset,
			final int limit) {
		logger.debug("getting user filters for class '{}' starting from '{}' with a limit of '{}'", className, offset,
				limit);
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(isUserFilter(), filtersAssociatedTo(userId), forClass(className))) //
				.offset(offset) //
				.limit(limit) //
				.orderBy(NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	private WhereClause forClass(final String name) {
		final WhereClause clause;
		if (name == null) {
			clause = alwaysTrue();
		} else {
			final CMClass entryType = view.findClass(name);
			clause = forClass(entryType);
		}
		return clause;
	}

	private WhereClause forClass(final CMEntryType entryType) {
		final WhereClause clause;
		if (entryType == null) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(getFilterClass(), ENTRYTYPE), eq(entryType.getId()));
		}
		return clause;
	}

	private WhereClause filtersAssociatedTo(final Long id) {
		final WhereClause clause;
		if (id == null) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(getFilterClass(), OWNER), eq(id));
		}
		return clause;
	}

	private WhereClause isUserFilter() {
		return condition(attribute(getFilterClass(), SHARED), eq(false));
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters(final String className, final int start, final int limit) {
		logger.debug("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(not(isUserFilter()), forClass(className))) //
				.offset(start) //
				.limit(limit) //
				.orderBy(NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public Long create(final Filter filter) {
		return converter.fill(view.createCardFor(getFilterClass()), filter) //
				.save() //
				.getId();
	}

	@Override
	public void update(final Filter filter) {
		converter.fill(view.update(card(filter.getId())), filter) //
				.save();
	}

	@Override
	public void delete(final Filter filter) {
		view.delete(card(filter.getId()));
		grantCleaner.deleteGrantReferingTo(filter.getId());
	}

	@Override
	public Long getPosition(final Filter filter) {
		final CMClass filterClass = getFilterClass();
		return view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.numbered(condition(attribute(filterClass, ID), eq(filter.getId()))) //
				.orderBy(NAME, ASC) //
				.run() //
				.getOnlyRow() //
				.getNumber();
	}

	private Function<CMCard, Filter> cardToFilter() {
		return new Function<CMCard, Filter>() {

			@Override
			public Filter apply(final CMCard input) {
				return converter.convert(input);
			}

		};
	}

	private CMCard card(final Long id) {
		logger.debug("getting filter card with id '{}'", id);
		final CMClass filterClass = getFilterClass();
		return view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, ID), eq(id))) //
				.limit(1) //
				.run() //
				.getOnlyRow() //
				.getCard(filterClass);
	}

}
