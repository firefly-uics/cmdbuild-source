package org.cmdbuild.logic.mappers.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mappers.WhereClauseBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

/**
 * Class that creates a WhereClause starting from a json string. This where
 * clause will "retrieve" the cards of the specified entry type that pass the
 * filter.
 */
public class JSONFilterBuilder implements WhereClauseBuilder {

	private final JSONObject filterObject;
	protected CMEntryType entryType;
	private static final String SIMPLE = "simple";
	private static final String AND = "and";
	private static final String OR = "or";
	private static final String ATTRIBUTE = "attribute";
	private static final String OPERATOR = "operator";
	private static final String VALUE = "value";

	/**
	 * 
	 * @param filterObject
	 *            the JSON object associated to the key "filter" of the global
	 *            filter
	 * @param entryType
	 */
	public JSONFilterBuilder(final JSONObject filterObject, final CMEntryType entryType) {
		Validate.notNull(filterObject);
		Validate.notNull(entryType);
		this.entryType = entryType;
		this.filterObject = filterObject;
	}

	@Override
	public WhereClause build() {
		try {
			return buildWhereClause(filterObject);
		} catch (final JSONException ex) {
			throw new IllegalArgumentException("The filter is malformed");
		}
	}

	protected WhereClause buildWhereClause(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(SIMPLE)) {
			final JSONObject simpleCondition = filterObject.getJSONObject(SIMPLE);
			final String attributeName = simpleCondition.getString(ATTRIBUTE);
			final String operator = simpleCondition.getString(OPERATOR);
			final JSONArray values = simpleCondition.getJSONArray(VALUE);
			return buildSimpleWhereClause(attribute(entryType, attributeName), operator, values);
		} else if (filterObject.has(AND)) {
			final JSONArray andConditions = filterObject.getJSONArray(AND);
			Validate.isTrue(andConditions.length() >= 2);
			final JSONObject firstAnd = andConditions.getJSONObject(0);
			final JSONObject secondAnd = andConditions.getJSONObject(1);
			return and(buildWhereClause(firstAnd), //
					buildWhereClause(secondAnd), //
					createOptionalWhereClauses(andConditions));
		} else if (filterObject.has(OR)) {
			final JSONArray orConditions = filterObject.getJSONArray(OR);
			Validate.isTrue(orConditions.length() >= 2);
			final JSONObject firstOr = orConditions.getJSONObject(0);
			final JSONObject secondOr = orConditions.getJSONObject(1);
			return or(buildWhereClause(firstOr), //
					buildWhereClause(secondOr), //
					createOptionalWhereClauses(orConditions));
		}
		throw new IllegalArgumentException("The filter is malformed");
	}

	private WhereClause buildSimpleWhereClause(final QueryAliasAttribute attribute, final String operator,
			final JSONArray values) throws JSONException {
		if (operator.equals("equal")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, eq(values.get(0)));
		} else if (operator.equals("notequal")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, eq(values.get(0))));
		} else if (operator.equals("null")) {
			Validate.isTrue(values.length() == 0);
			return condition(attribute, isNull());
		} else if (operator.equals("notnull")) {
			Validate.isTrue(values.length() == 0);
			return not(condition(attribute, isNull()));
		} else if (operator.equals("greater")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, gt(values.get(0)));
		} else if (operator.equals("less")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, lt(values.get(0)));
		} else if (operator.equals("between")) {
			Validate.isTrue(values.length() == 2);
			and(condition(attribute, gt(values.get(0))), condition(attribute, lt(values.get(1))));
		} else if (operator.equals("like")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(values.get(0)));
		} else if (operator.equals("contain")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(values.get(0)));
		} else if (operator.equals("notcontain")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, contains(values.get(0))));
		} else if (operator.equals("begin")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, beginsWith(values.get(0)));
		} else if (operator.equals("notbegin")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, beginsWith(values.get(0))));
		} else if (operator.equals("end")) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, endsWith(values.get(0)));
		} else if (operator.equals("notend")) {
			Validate.isTrue(values.length() == 1);
			return not(condition(attribute, endsWith(values.get(0))));
		}
		throw new IllegalArgumentException("The operator " + operator + " is not supported");
	}

	private WhereClause[] createOptionalWhereClauses(final JSONArray conditions) throws JSONException {
		final List<WhereClause> optionalWhereClauses = Lists.newArrayList();
		for (int i = 2; i < conditions.length(); i++) {
			final JSONObject andCond = conditions.getJSONObject(i);
			optionalWhereClauses.add(buildWhereClause(andCond));
		}
		return optionalWhereClauses.toArray(new WhereClause[optionalWhereClauses.size()]);
	}

}
