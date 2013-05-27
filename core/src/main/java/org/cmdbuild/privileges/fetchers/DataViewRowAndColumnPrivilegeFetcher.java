package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext.PrivilegedObjectMetadata;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DataViewRowAndColumnPrivilegeFetcher implements RowAndColumnPrivilegeFetcher {

	private final CMDataView view;
	private final PrivilegeContext privilegeContext;

	public DataViewRowAndColumnPrivilegeFetcher(final CMDataView view, final PrivilegeContext privilegeContext) {
		this.view = view;
		this.privilegeContext = privilegeContext;
	}

	/**
	 * FIXME: consider also filter on relations... bug on privileges on rows
	 * when relations are specified
	 */
	@Override
	public WhereClause fetchPrivilegeFiltersFor(final CMEntryType entryType) {
		if (privilegeContext.hasAdministratorPrivileges() && entryType.isActive()) {
			return trueWhereClause();
		}
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		if (metadata == null) {
			return trueWhereClause();
		}
		final List<String> privilegeFilters = metadata.getFilters();
		final List<WhereClause> whereClauseFilters = Lists.newArrayList();
		for (final String privilegeFilter : privilegeFilters) {
			try {
				final WhereClause whereClause = createWhereClauseFrom(privilegeFilter, entryType);
				whereClauseFilters.add(whereClause);
			} catch (final JSONException ex) {
				// TODO: log
			}
		}
		return createGlobalOrWhereClauseFrom(whereClauseFilters, entryType);
	}

	private WhereClause createWhereClauseFrom(final String privilegeFilter, final CMEntryType entryType)
			throws JSONException {
		final JSONObject jsonPrivilegeFilter = new JSONObject(privilegeFilter);
		final FilterMapper filterMapper = JsonFilterMapper.newInstance() //
				.withDataView(view) //
				.withEntryType(entryType) //
				.withFilterObject(jsonPrivilegeFilter) //
				.build();
		return filterMapper.whereClause();
	}

	private WhereClause createGlobalOrWhereClauseFrom(final List<WhereClause> whereClauses, final CMEntryType entryType) {
		if (whereClauses.isEmpty()) {
			return condition(attribute(entryType, SystemAttributes.IdClass.getDBName()), eq(entryType.getId()));
		} else if (whereClauses.size() == 1) {
			return whereClauses.get(0);
		} else if (whereClauses.size() == 2) {
			return or(whereClauses.get(0), whereClauses.get(1));
		} else {
			final WhereClause[] otherWhereClauses = whereClauses.subList(2, whereClauses.size()).toArray(
					new WhereClause[whereClauses.size() - 2]);
			return or(whereClauses.get(0), whereClauses.get(1), otherWhereClauses);
		}
	}

	@Override
	public Iterable<String> fetchDisabledAttributesFor(final CMEntryType entryType) {
		final List<String> disabledAttributes = Lists.newArrayList();
		if (privilegeContext.hasAdministratorPrivileges()) {
			return disabledAttributes;
		}
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		if (metadata == null) {
			return disabledAttributes;
		}
		return metadata.getDisabledAttributes();
	}
}
