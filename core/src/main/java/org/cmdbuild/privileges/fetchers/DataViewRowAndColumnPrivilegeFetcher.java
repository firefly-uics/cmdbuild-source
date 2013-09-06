package org.cmdbuild.privileges.fetchers;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.TrueWhereClause.trueWhereClause;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext.PrivilegedObjectMetadata;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMAttribute;
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

	private final CMDataView dataView;
	private final PrivilegeContext privilegeContext;

	public DataViewRowAndColumnPrivilegeFetcher( //
			final CMDataView dataView, //
			final PrivilegeContext privilegeContext //
	) {
		this.dataView = dataView;
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
				.withDataView(dataView) //
				.withDataView(dataView) //
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
	/**
	 * If superUser return write privilege
	 * for all the attributes
	 * 
	 * If not superUser, looking for
	 * some attributes privilege
	 * definition, if there is no one
	 * return the attributes mode
	 * defined globally
	 */
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {

		final Map<String, String> groupLevelAttributePrivileges = getAttributePrivilegesMap(entryType);

		// initialize a map with the
		// mode set for attribute globally
		final Map<String, String> mergedAttributesPrivileges = new HashMap<String, String>();
		final Iterable<? extends CMAttribute> attributes = entryType.getAllAttributes();
		for (final CMAttribute attribute: attributes) {
			if (attribute.isActive()) {
				final String mode = attribute.getMode().name().toLowerCase();
				mergedAttributesPrivileges.put(attribute.getName(), mode);
			}
		}

		/*
		 * The super user has no added limitation
		 * for the attributes, so return the
		 * global attributes modes
		 */
		if (privilegeContext.hasAdministratorPrivileges()) {
		//	return attributesPrivilegesForAdmin(entryType);
			return mergedAttributesPrivileges;
		}

		// merge with the privileges set at group level
		for (final String attributeName: groupLevelAttributePrivileges.keySet()) {
			if (mergedAttributesPrivileges.containsKey(attributeName)) {
				mergedAttributesPrivileges.put( //
						attributeName, //
						groupLevelAttributePrivileges.get(attributeName) //
					);
			}
		}

		return mergedAttributesPrivileges;
	}

	/*
	 * get write privileges to all the
	 * active attributes 
	 */
	private Map<String, String> attributesPrivilegesForAdmin(final CMEntryType entryType) {
		final Map<String, String> privileges = new HashMap<String, String>();
		for (final CMAttribute attribute: entryType.getActiveAttributes()) {
			if (attribute.isActive()) {
				privileges.put(attribute.getName(), "write");
			}
		}

		return privileges;
	}

	private Map<String, String> getAttributePrivilegesMap(final CMEntryType entryType) {
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		final Map<String, String> attributePrivileges = new HashMap<String, String>();
		if (metadata != null) {
			for (final String privilege: metadata.getAttributesPrivileges()) {
				final String[] parts = privilege.split(":");
				attributePrivileges.put(parts[0], parts[1]);
			}
		}

		return attributePrivileges;
	}
}
