package org.cmdbuild.privileges.fetchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.auth.acl.PrivilegeContext.PrivilegedObjectMetadata;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.dao.view.user.privileges.RowAndColumnPrivilegeFetcher;
import org.cmdbuild.logic.mapping.json.JsonFilterMapper;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class DataViewRowAndColumnPrivilegeFetcher implements RowAndColumnPrivilegeFetcher {

	private static final Iterable<? extends WhereClause> EMPTY_WHERE_CLAUSES = Collections.emptyList();

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
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType) {
		return fetchPrivilegeFiltersFor(entryType, entryType);
	}

	@Override
	public Iterable<? extends WhereClause> fetchPrivilegeFiltersFor(final CMEntryType entryType,
			final CMEntryType entryTypeForClauses) {
		if (privilegeContext.hasAdministratorPrivileges() && entryType.isActive()) {
			return EMPTY_WHERE_CLAUSES;
		}
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		if (metadata == null) {
			return EMPTY_WHERE_CLAUSES;
		}
		final List<String> privilegeFilters = metadata.getFilters();
		final List<WhereClause> whereClauseFilters = Lists.newArrayList();
		for (final String privilegeFilter : privilegeFilters) {
			try {
				final WhereClause whereClause = createWhereClauseFrom(privilegeFilter, entryTypeForClauses);
				whereClauseFilters.add(whereClause);
			} catch (final JSONException ex) {
				// TODO: log
			}
		}
		return whereClauseFilters;
	}

	private WhereClause createWhereClauseFrom(final String privilegeFilter, final CMEntryType entryType)
			throws JSONException {
		final JSONObject jsonPrivilegeFilter = new JSONObject(privilegeFilter);
		return JsonFilterMapper.newInstance() //
				.withDataView(view) //
				.withEntryType(entryType) //
				.withFilterObject(jsonPrivilegeFilter) //
				.build() //
				.whereClause();
	}

	/**
	 * If superUser return write privilege for all the attributes
	 * 
	 * If not superUser, looking for some attributes privilege definition, if
	 * there is no one return the attributes mode defined globally
	 */
	@Override
	public Map<String, String> fetchAttributesPrivilegesFor(final CMEntryType entryType) {

		final Map<String, String> groupLevelAttributePrivileges = getAttributePrivilegesMap(entryType);

		// initialize a map with the
		// mode set for attribute globally
		final Map<String, String> mergedAttributesPrivileges = new HashMap<String, String>();
		final Iterable<? extends CMAttribute> attributes = entryType.getAllAttributes();
		for (final CMAttribute attribute : attributes) {
			if (attribute.isActive()) {
				final String mode = attribute.getMode().name().toLowerCase();
				mergedAttributesPrivileges.put(attribute.getName(), mode);
			}
		}

		/*
		 * The super user has no added limitation for the attributes, so return
		 * the global attributes modes
		 */
		if (privilegeContext.hasAdministratorPrivileges()) {
			return mergedAttributesPrivileges;
		}

		// merge with the privileges set at group level
		for (final String attributeName : groupLevelAttributePrivileges.keySet()) {
			if (mergedAttributesPrivileges.containsKey(attributeName)) {
				mergedAttributesPrivileges.put( //
						attributeName, //
						groupLevelAttributePrivileges.get(attributeName) //
						);
			}
		}

		return mergedAttributesPrivileges;
	}

	private Map<String, String> getAttributePrivilegesMap(final CMEntryType entryType) {
		final PrivilegedObjectMetadata metadata = privilegeContext.getMetadata(entryType);
		final Map<String, String> attributePrivileges = new HashMap<String, String>();
		if (metadata != null) {
			for (final String privilege : metadata.getAttributesPrivileges()) {
				final String[] parts = privilege.split(":");
				attributePrivileges.put(parts[0], parts[1]);
			}
		}

		return attributePrivileges;
	}
}
