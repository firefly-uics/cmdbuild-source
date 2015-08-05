package org.cmdbuild.services.store.filter;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Iterables.concat;
import static org.cmdbuild.dao.guava.Functions.toCard;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.OrderByClause.Direction.ASC;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.CMUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.dao.StorableConverter;
import org.cmdbuild.privileges.GrantCleaner;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

public class DataViewFilterStore implements FilterStore {

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

	@Override
	public Filter fetchFilter(final Long filterId) {
		return cardToFilter().apply(card(filterId));
	}

	@Override
	public PagedElements<Filter> getAllUserFilters(final String className, final int offset, final int limit) {
		logger.debug("getting all filters");
		final CMClass clazz = view.findClass(className);
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(isUserFilter(), matchTable(clazz))) //
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

	private WhereClause matchTable(final CMClass entryType) {
		return condition(attribute(getFilterClass(), ENTRYTYPE_ATTRIBUTE_NAME), eq(entryType.getId()));
	}

	@Override
	public PagedElements<Filter> getAllUserFilters() {
		logger.debug("getting all user filters");
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(isUserFilter()) //
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter());
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	/**
	 * Retrieves all filters that the user can see (filters defined by itself
	 * and readable group filters)
	 */
	@Override
	public PagedElements<Filter> getFiltersForCurrentlyLoggedUser(final String className) {
		logger.debug("getting all filters");
		final CMUser user = operationUser.getAuthenticatedUser();
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(and(onlyEntryTypeWithName(className), filtersAssociatedTo(user), isUserFilter())) //
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter());
		final Iterable<Filter> filters2 = from(fetchAllGroupsFilters()) //
				.filter(new Predicate<Filter>() {

					@Override
					public boolean apply(final Filter input) {
						return (input.getClassName().equals(className) && (operationUser.hasAdministratorPrivileges() || operationUser
								.hasReadAccess(input)));
					}

				});
		final Iterable<Filter> allFilters = concat(filters, filters2);
		return new PagedElements<Filter>(allFilters, result.totalSize());
	}

	private WhereClause onlyEntryTypeWithName(final String entryTypeName) {
		final WhereClause clause;
		if (entryTypeName == null) {
			clause = trueWhereClause();
		} else {
			final CMClass entryType = view.findClass(entryTypeName);
			if (entryType == null) {
				clause = trueWhereClause();
			} else {
				clause = condition(attribute(getFilterClass(), ENTRYTYPE_ATTRIBUTE_NAME), eq(entryType.getId()));
			}
		}
		return clause;
	}

	private WhereClause filtersAssociatedTo(final CMUser user) {
		final WhereClause clause;
		if (user == null) {
			clause = trueWhereClause();
		} else {
			clause = condition(attribute(getFilterClass(), MASTER_ATTRIBUTE_NAME), eq(user.getId()));
		}
		return clause;
	}

	private WhereClause isUserFilter() {
		return condition(attribute(getFilterClass(), TEMPLATE_ATTRIBUTE_NAME), eq(false));
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters() {
		logger.debug("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, TEMPLATE_ATTRIBUTE_NAME), eq(true))) //
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter()) //
				.filter(new Predicate<Filter>() {

					@Override
					public boolean apply(final Filter input) {
						return operationUser.hasReadAccess(input);
					}

				});
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public PagedElements<Filter> fetchAllGroupsFilters(final int start, final int limit) {
		logger.debug("getting all filter cards");
		final CMClass filterClass = getFilterClass();
		final CMQueryResult result = view.select(anyAttribute(filterClass)) //
				.from(filterClass) //
				.where(condition(attribute(filterClass, TEMPLATE_ATTRIBUTE_NAME), eq(true))) //
				.offset(start) //
				.limit(limit) //
				.orderBy(NAME_ATTRIBUTE_NAME, ASC) //
				.count() //
				.run();
		final Iterable<Filter> filters = from(result) //
				.transform(toCard(filterClass)) //
				.transform(cardToFilter()) //
				.filter(new Predicate<Filter>() {

					@Override
					public boolean apply(final Filter input) {
						return operationUser.hasReadAccess(input);
					}

				});
		return new PagedElements<Filter>(filters, result.totalSize());
	}

	@Override
	public Long create(final Filter filter) {
		Validate.notNull(filter.getClassName());
		final CMClass clazz = view.findClass(filter.getClassName());
		return view.createCardFor(getFilterClass()) //
				.set(MASTER_ATTRIBUTE_NAME, operationUser.getAuthenticatedUser().getId()) //
				.set(NAME_ATTRIBUTE_NAME, filter.getName()) //
				.set(DESCRIPTION_ATTRIBUTE_NAME, filter.getDescription()) //
				.set(FILTER_ATTRIBUTE_NAME, filter.getValue()) //
				.set(TEMPLATE_ATTRIBUTE_NAME, filter.isTemplate()) //
				.set(ENTRYTYPE_ATTRIBUTE_NAME, clazz.getId()) //
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
