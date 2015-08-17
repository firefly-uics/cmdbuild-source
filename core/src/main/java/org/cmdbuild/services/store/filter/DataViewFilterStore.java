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

import org.apache.commons.lang3.Validate;
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

	protected static final String FILTERS_CLASS_NAME = "_Filter";
	private static final String ID_ATTRIBUTE_NAME = "Id";
	protected static final String NAME_ATTRIBUTE_NAME = "Code";
	protected static final String DESCRIPTION_ATTRIBUTE_NAME = "Description";
	protected static final String FILTER_ATTRIBUTE_NAME = "Filter";
	protected static final String ENTRYTYPE_ATTRIBUTE_NAME = "IdSourceClass";
	protected static final String TEMPLATE_ATTRIBUTE_NAME = "Template";
	public static final String MASTER_ATTRIBUTE_NAME = "IdOwner";

	private final CMDataView view;
	private final GrantCleaner grantCleaner;
	private final StorableConverter<FilterStore.Filter> converter;

	public DataViewFilterStore(final CMDataView dataView, final StorableConverter<FilterStore.Filter> converter) {
		this.view = dataView;
		this.grantCleaner = new GrantCleaner(view);
		this.converter = converter;
	}

	public CMClass getFilterClass() {
		return view.findClass(FILTERS_CLASS_NAME);
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
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
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
			clause = condition(attribute(getFilterClass(), ENTRYTYPE_ATTRIBUTE_NAME), eq(entryType.getId()));
		}
		return clause;
	}

	private WhereClause filtersAssociatedTo(final Long id) {
		final WhereClause clause;
		if (id == null) {
			clause = alwaysTrue();
		} else {
			clause = condition(attribute(getFilterClass(), MASTER_ATTRIBUTE_NAME), eq(id));
		}
		return clause;
	}

	private WhereClause isUserFilter() {
		return condition(attribute(getFilterClass(), TEMPLATE_ATTRIBUTE_NAME), eq(false));
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
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public Long create(final Filter filter) {
		Validate.notNull(filter.getClassName());
		final CMClass clazz = view.findClass(filter.getClassName());
		return view.createCardFor(getFilterClass()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(TEMPLATE_ATTRIBUTE_NAME, filter.isTemplate()) //
				.set(MASTER_ATTRIBUTE_NAME, filter.getOwner()) //
				.save() //
				.getId();
	}

	@Override
	public Filter update(final Filter filter) {
		final CMClass clazz = view.findClass(filter.getClassName());
		final CMCard updated = view.update(card(filter.getId())) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId()) //
				.save();
		return cardToFilter().apply(updated);
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
				.numbered(condition(attribute(filterClass, ID_ATTRIBUTE_NAME), eq(filter.getId()))) //
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
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
				.where(condition(attribute(filterClass, ID_ATTRIBUTE_NAME), eq(id))) //
				.limit(1) //
				.run() //
				.getOnlyRow() //
				.getCard(filterClass);
	}

}
