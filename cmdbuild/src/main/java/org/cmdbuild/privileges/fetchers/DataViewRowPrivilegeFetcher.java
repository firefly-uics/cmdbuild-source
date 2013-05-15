package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.dao.query.clause.where.FalseWhereClause.falseWhereClause;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.List;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext.PrivilegedObjectMetadata;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowPrivilegeFetcher;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DataViewRowPrivilegeFetcher implements RowPrivilegeFetcher {

	private final CMDataView view;
	private final PrivilegeContext privilegeContext;

	public DataViewRowPrivilegeFetcher(final CMDataView view, final PrivilegeContext privilegeContext) {
		this.view = view;
		this.privilegeContext = privilegeContext;
	}

	@Override
	public WhereClause fetchPrivilegeFiltersFor(final CMClass entryType) {
		if (privilegeContext.hasAdministratorPrivileges()) {
			return trueWhereClause();
		}
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		if (metadata == null) {
			return falseWhereClause();
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
		return createGlobalOrWhereClauseFrom(whereClauseFilters);
	}

	private WhereClause createWhereClauseFrom(final String privilegeFilter, final CMClass entryType)
			throws JSONException {
		final JSONObject jsonPrivilegeFilter = new JSONObject(privilegeFilter);
		final FilterMapper filterMapper = JsonFilterMapper.newInstance() //
				.withDataView(view) //
				.withEntryType(entryType) //
				.withFilterObject(jsonPrivilegeFilter) //
				.build();
		return filterMapper.whereClause();
	}

	private WhereClause createGlobalOrWhereClauseFrom(final List<WhereClause> whereClauses) {
		if (whereClauses.isEmpty()) {
			return trueWhereClause();
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
}
