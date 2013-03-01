package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.NULL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_SOURCE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.TrueWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
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

	private static final JSONArray EMPTY_VALUES = new JSONArray();

	private static final Logger logger = Log.CMDBUILD;

	private final CMEntryType entryType;
	private final JSONObject filterObject;
	private final Validator filterValidator;
	private final CMDataView dataView;

	public JsonFilterMapper(final CMEntryType entryType, final JSONObject filterObject, final CMDataView dataView) {
		Validate.notNull(entryType);
		Validate.notNull(filterObject);
		this.entryType = entryType;
		this.filterObject = filterObject;
		this.filterValidator = new JsonFilterValidator(filterObject);
		this.dataView = dataView;
	}

	@Override
	public WhereClause whereClause() {
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
//			return new TrueWhereClause();
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
			whereClauseBuilders.add(new JsonFilterBuilder(filterObject.getJSONObject(ATTRIBUTE_KEY), entryType,
					dataView));
		}
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			whereClauseBuilders
					.add(new JsonFullTextQueryBuilder(filterObject.getString(FULL_TEXT_QUERY_KEY), entryType));
		}
		if (filterObject.has(RELATION_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {
				final JSONObject condition = conditions.getJSONObject(i);
				if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_ONEOF)) {
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);

					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, IN.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						final Long id = card.getLong(RELATION_CARD_ID_KEY);
						simple.append(VALUE_KEY, id);
					}

					whereClauseBuilders.add(new JsonFilterBuilder(filter, entryType, dataView));
				} else if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE)) {
					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, NULL.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));
					simple.put(VALUE_KEY, EMPTY_VALUES);

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					whereClauseBuilders.add(new JsonFilterBuilder(filter, entryType, dataView));

				}
			}
		}
		return whereClauseBuilders;
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info("getting join elements for filter");
		final List<JoinElement> joinElements = Lists.newArrayList();
		if (filterObject.has(RELATION_KEY)) {
			try {
				final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
				for (int i = 0; i < conditions.length(); i++) {
					final JSONObject condition = conditions.getJSONObject(i);
					final String domain = condition.getString(RELATION_DOMAIN_KEY);
					final String source = condition.getString(RELATION_SOURCE_KEY);
					final String destination = condition.getString(RELATION_DESTINATION_KEY);
					final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
					joinElements.add(JoinElement.newInstance(domain, source, destination, left));
				}
			} catch (final Exception e) {
				logger.error("error getting json element", e);
			}
		}
		return joinElements;
	}

}
