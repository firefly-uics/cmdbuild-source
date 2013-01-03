package org.cmdbuild.logic.mappers.json;

import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mappers.FilterMapper;
import org.cmdbuild.logic.mappers.WhereClauseBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONFilterMapper implements FilterMapper {

	private static final String FILTER = "filter";
	private static final String FULL_TEXT_QUERY = "fullTextQuery";
	private final CMEntryType entryType;
	private final JSONObject globalFilterObject;
	private List<WhereClauseBuilder> whereClauseBuilders;

	public JSONFilterMapper(final CMEntryType entryType, final JSONObject globalFilterObject) {
		Validate.notNull(entryType);
		Validate.notNull(globalFilterObject);
		this.entryType = entryType;
		this.globalFilterObject = globalFilterObject;
		try {
			whereClauseBuilders = getWhereClauseBuildersForFilter();
		} catch (final JSONException ex) {
			throw new IllegalArgumentException("Malformed filter");
		}
	}

	private List<WhereClauseBuilder> getWhereClauseBuildersForFilter() throws JSONException {
		if (globalFilterIsValid()) {
			final List<WhereClauseBuilder> whereClauseBuilders = Lists.newArrayList();
			if (globalFilterObject.has(FILTER)) {
				whereClauseBuilders.add(new JSONFilterBuilder(globalFilterObject.getJSONObject(FILTER), entryType));
			}
			if (globalFilterObject.has(FULL_TEXT_QUERY)) {
				whereClauseBuilders.add(new JSONFullTextQueryBuilder(globalFilterObject.getString(FULL_TEXT_QUERY),
						entryType));
			}
			return whereClauseBuilders;
		} else {
			throw new IllegalArgumentException();
		}
	}

	private boolean globalFilterIsValid() {
		final boolean emptyFilter = globalFilterObject.length() == 0;
		if (emptyFilter) {
			return true;
		}
		if (!globalFilterObject.has(FILTER) && !globalFilterObject.has(FULL_TEXT_QUERY)) {
			return false;
		}
		return true;
	}

	@Override
	public WhereClause deserialize() {
		final WhereClause[] whereClauses = new WhereClause[whereClauseBuilders.size()];
		for (int i = 0; i < whereClauses.length; i++) {
			whereClauses[i] = whereClauseBuilders.get(i).build();
		}
		if (whereClauses.length == 0) {
			return new EmptyWhereClause();
		} else if (whereClauses.length == 1) {
			return whereClauses[0];
		} else if (whereClauses.length == 2) {
			return and(whereClauses[0], whereClauses[1]);
		} else {
			return and(whereClauses[0], whereClauses[1], Arrays.copyOfRange(whereClauses, 2, whereClauses.length - 1));
		}
	}

}
