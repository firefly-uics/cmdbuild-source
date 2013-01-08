package org.cmdbuild.logic.mappers.json;

import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.logic.mappers.json.Constants.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mappers.FilterMapper;
import org.cmdbuild.logic.mappers.WhereClauseBuilder;
import org.cmdbuild.logic.validators.JSONFilterValidator;
import org.cmdbuild.logic.validators.Validator;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JSONFilterMapper implements FilterMapper {

	private final CMEntryType entryType;
	private final JSONObject filterObject;
	private final Validator filterValidator;

	public JSONFilterMapper(final CMEntryType entryType, final JSONObject filterObject) {
		Validate.notNull(entryType);
		Validate.notNull(filterObject);
		this.entryType = entryType;
		this.filterObject = filterObject;
		this.filterValidator = new JSONFilterValidator(filterObject);
	}

	@Override
	public WhereClause deserialize() {
		filterValidator.validate();

		List<WhereClauseBuilder> whereClauseBuilders;
		try {
			whereClauseBuilders = getWhereClauseBuildersForFilter();
		} catch (final JSONException ex) {
			throw new IllegalArgumentException("Malformed filter");
		}

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

	private List<WhereClauseBuilder> getWhereClauseBuildersForFilter() throws JSONException {
		final List<WhereClauseBuilder> whereClauseBuilders = Lists.newArrayList();
		if (filterObject.has(ATTRIBUTE_KEY)) {
			whereClauseBuilders.add(new JSONFilterBuilder(filterObject.getJSONObject(ATTRIBUTE_KEY), entryType));
		}
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			whereClauseBuilders.add(new JSONFullTextQueryBuilder(filterObject.getString(FULL_TEXT_QUERY_KEY), entryType));
		}
		return whereClauseBuilders;
	}

}
