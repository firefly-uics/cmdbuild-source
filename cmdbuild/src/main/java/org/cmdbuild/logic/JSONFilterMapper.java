package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONFilterMapper implements FilterMapper {

	private static final String FILTER = "filter";
	private static final String FULL_TEXT_QUERY = "fullTextQuery";
	private CMEntryType entryType;
	private String globalFilter;
	private List<WhereClauseBuilder> whereClauseBuilders;

	public JSONFilterMapper(String globalFilter, CMEntryType entryType) {
		Validate.notNull(globalFilter);
		Validate.notNull(entryType);
		this.entryType = entryType;
		this.globalFilter = globalFilter;
		try {
			whereClauseBuilders = getWhereClauseBuildersForFilter();
		} catch (JSONException ex) {
			throw new IllegalArgumentException("Malformed filter");
		}
	}

	private List<WhereClauseBuilder> getWhereClauseBuildersForFilter() throws JSONException {
		List<WhereClauseBuilder> whereClauseBuilders = Lists.newArrayList();
		JSONObject globalFilterObject = new JSONObject(globalFilter);
		if (globalFilterObject.has(FILTER)) {
			whereClauseBuilders.add(new JSONFilterBuilder(globalFilterObject.getJSONObject(FILTER), entryType));
		}
		if (globalFilterObject.has(FULL_TEXT_QUERY)) {
			whereClauseBuilders.add(new JSONFullTextQueryBuilder(globalFilterObject.getString(FULL_TEXT_QUERY),
					entryType));
		}
		return whereClauseBuilders;
	}

	@Override
	public WhereClause deserialize() {
		WhereClause[] whereClauses = new WhereClause[whereClauseBuilders.size()];
		for (int i = 0; i < whereClauses.length; i++) {
			whereClauses[i] = whereClauseBuilders.get(i).build();
		}
		if (whereClauses.length == 0) {
			throw new IllegalArgumentException("Malformed filter");
		} else if (whereClauses.length == 1) {
			return whereClauses[0];
		} else if (whereClauses.length == 2) {
			return and(whereClauses[0], whereClauses[1]);
		} else {
			return and(whereClauses[0], whereClauses[1], Arrays.copyOfRange(whereClauses, 2, whereClauses.length - 1));
		}
	}

}
