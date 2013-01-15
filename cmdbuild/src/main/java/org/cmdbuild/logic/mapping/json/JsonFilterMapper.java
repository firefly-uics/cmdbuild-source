package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.logic.mapping.json.Constants.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.SRC_KEY;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.mapping.WhereClauseBuilder;
import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class JsonFilterMapper implements FilterMapper {

	private static final Logger logger = Log.CMDBUILD;

	private final CMEntryType entryType;
	private final JSONObject filterObject;
	private final Validator filterValidator;

	public JsonFilterMapper(final CMEntryType entryType, final JSONObject filterObject) {
		Validate.notNull(entryType);
		Validate.notNull(filterObject);
		this.entryType = entryType;
		this.filterObject = filterObject;
		this.filterValidator = new JsonFilterValidator(filterObject);
	}

	@Override
	public WhereClause whereClauses() {
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
			whereClauseBuilders.add(new JsonFilterBuilder(filterObject.getJSONObject(ATTRIBUTE_KEY), entryType));
		}
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			whereClauseBuilders
					.add(new JsonFullTextQueryBuilder(filterObject.getString(FULL_TEXT_QUERY_KEY), entryType));
		}
		// add here relations filter builder
		return whereClauseBuilders;
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info("getting json elements for filter");
		final List<JoinElement> joinElements = Lists.newArrayList();
		if (filterObject.has(RELATION_KEY)) {
			try {
				final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
				for (int i = 0; i < conditions.length(); i++) {
					final JSONObject condition = conditions.getJSONObject(i);
					final String domain = condition.getString(DOMAIN_KEY);
					final String source = condition.getString(SRC_KEY);
					joinElements.add(JoinElement.newInstance(domain, source));
				}
			} catch (final Exception e) {
				logger.error("error getting json element", e);
			}
		}
		return joinElements;
	}

}
