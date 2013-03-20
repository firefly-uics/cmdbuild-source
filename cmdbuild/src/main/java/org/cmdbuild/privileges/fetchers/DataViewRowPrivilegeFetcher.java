package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.auth.privileges.constants.GrantConstants.GRANT_CLASS_NAME;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.GROUP_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.MODE_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGED_CLASS_ID_ATTRIBUTE;
import static org.cmdbuild.auth.privileges.constants.GrantConstants.PRIVILEGE_FILTER_ATTRIBUTE;
import static org.cmdbuild.dao.query.clause.AnyAttribute.anyAttribute;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.cmdbuild.auth.privileges.constants.PrivilegeMode;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.CMQueryRow;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowPrivilegeFetcher;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class DataViewRowPrivilegeFetcher implements RowPrivilegeFetcher {

	private final CMDataView view;
	private final OperationUser operationUser;
	private final CMClass grantClass;

	public DataViewRowPrivilegeFetcher(final CMDataView view, final OperationUser operationUser) {
		this.view = view;
		this.operationUser = operationUser;
		this.grantClass = view.findClass(GRANT_CLASS_NAME);
	}

	@Override
	public WhereClause fetchPrivilegeFiltersFor(final CMClass entryType) {
		final boolean userHasDefaultGroup = operationUser.getAuthenticatedUser().getDefaultGroupName() != null;
		WhereClause whereClause = null;
		if (!userHasDefaultGroup) {
			try {
				whereClause = fetchPrivilegeFilterForLoggedGroup(entryType);
			} catch (final JSONException ex) {
				throw new IllegalArgumentException("Malformed filter");
			}
		} else {
			try {
				whereClause = fetchPrivilegeFiltersForDefaultGroup(entryType);
			} catch (final JSONException ex) {
				throw new IllegalArgumentException("Malformed filter");
			}
		}
		return whereClause;
	}

	private WhereClause fetchPrivilegeFilterForLoggedGroup(final CMClass entryType) throws JSONException {
		if (operationUser.hasAdministratorPrivileges()) {
			return new TrueWhereClause();
		}
		final Long groupIdUsedForLogin = operationUser.getPreferredGroup().getId();
		final CMQueryRow row = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), eq(groupIdUsedForLogin)),
						condition(attribute(grantClass, PRIVILEGED_CLASS_ID_ATTRIBUTE), eq(entryType.getId())))).run()
				.getOnlyRow();
		final Object privilegeFilter = row.getCard(grantClass).get(PRIVILEGE_FILTER_ATTRIBUTE);
		if (privilegeFilter == null) {
			return new TrueWhereClause();
		} else {
			final JSONObject jsonPrivilegeFilter = new JSONObject((String) privilegeFilter);
			final FilterMapper filterMapper = new JsonFilterMapper(entryType, jsonPrivilegeFilter, view);
			return filterMapper.whereClause();
		}
	}

	private WhereClause fetchPrivilegeFiltersForDefaultGroup(final CMClass entryType) throws JSONException {
		if (operationUser.hasAdministratorPrivileges()) {
			return new TrueWhereClause();
		}
		final Iterable<Long> groupIds = getGroupIdsFromGroupNames(operationUser.getAuthenticatedUser().getGroupNames());
		final Object[] groupIdsArray = Iterables.toArray(groupIds, Long.class);
		final CMQueryResult result = view
				.select(anyAttribute(grantClass))
				.from(grantClass)
				.where(and(condition(attribute(grantClass, GROUP_ID_ATTRIBUTE), in(groupIdsArray)),
						condition(attribute(grantClass, PRIVILEGED_CLASS_ID_ATTRIBUTE), eq(entryType.getId())),
						not(condition(attribute(grantClass, MODE_ATTRIBUTE), eq(PrivilegeMode.NONE.getValue())))))
				.run();
		final List<WhereClause> whereClauses = Lists.newArrayList();
		for (final CMQueryRow row : result) {
			final Object privilegeFilter = row.getCard(grantClass).get(PRIVILEGE_FILTER_ATTRIBUTE);
			if (privilegeFilter == null) {
				return new TrueWhereClause();
			} else {
				final JSONObject jsonPrivilegeFilter = new JSONObject((String) privilegeFilter);
				final FilterMapper filterMapper = new JsonFilterMapper(entryType, jsonPrivilegeFilter, view);
				whereClauses.add(filterMapper.whereClause());
			}
		}
		return createOrWhereClauseFrom(whereClauses);
	}

	private Iterable<Long> getGroupIdsFromGroupNames(final Iterable<String> groupNames) {
		final Iterable<CMCard> groupCards = fetchGroupCardsFromNames(groupNames);
		final List<Long> groupIds = Lists.newArrayList();
		for (final CMCard groupCard : groupCards) {
			groupIds.add(groupCard.getId());
		}
		return groupIds;
	}

	private Iterable<CMCard> fetchGroupCardsFromNames(final Iterable<String> groupNames) {
		final List<CMCard> groupCardsToReturn = Lists.newArrayList();
		final CMClass roleClass = view.findClass("Role");
		final Object[] groupNamesArray = Iterables.toArray(groupNames, String.class);
		final CMQueryResult result = view.select(anyAttribute(roleClass)) //
				.from(roleClass) //
				.where(condition(attribute(roleClass, "Code"), in(groupNamesArray))) //
				.run();
		for (final CMQueryRow row : result) {
			groupCardsToReturn.add(row.getCard(roleClass));
		}
		return groupCardsToReturn;
	}

	private WhereClause createOrWhereClauseFrom(final List<WhereClause> whereClauses) {
		if (whereClauses.isEmpty()) {
			return new TrueWhereClause();
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
