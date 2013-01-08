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
import static org.cmdbuild.logic.mappers.json.Constants.AND_KEY;
import static org.cmdbuild.logic.mappers.json.Constants.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mappers.json.Constants.OPERATOR_KEY;
import static org.cmdbuild.logic.mappers.json.Constants.OR_KEY;
import static org.cmdbuild.logic.mappers.json.Constants.SIMPLE_KEY;
import static org.cmdbuild.logic.mappers.json.Constants.VALUE_KEY;

import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mappers.WhereClauseBuilder;
import org.cmdbuild.logic.mappers.json.Constants.FilterOperator;
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
	private final CMEntryType entryType;

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
		if (filterObject.has(SIMPLE_KEY)) {
			final JSONObject simpleCondition = filterObject.getJSONObject(SIMPLE_KEY);
			final String attributeName = simpleCondition.getString(ATTRIBUTE_KEY);
			final String operator = simpleCondition.getString(OPERATOR_KEY);
			final JSONArray values = simpleCondition.getJSONArray(VALUE_KEY);
			return buildSimpleWhereClause(attribute(entryType, attributeName), operator, values);
		} else if (filterObject.has(AND_KEY)) {
			final JSONArray andConditions = filterObject.getJSONArray(AND_KEY);
			Validate.isTrue(andConditions.length() >= 2);
			final JSONObject firstAnd = andConditions.getJSONObject(0);
			final JSONObject secondAnd = andConditions.getJSONObject(1);
			return and(buildWhereClause(firstAnd), //
					buildWhereClause(secondAnd), //
					createOptionalWhereClauses(andConditions));
		} else if (filterObject.has(OR_KEY)) {
			final JSONArray orConditions = filterObject.getJSONArray(OR_KEY);
			Validate.isTrue(orConditions.length() >= 2);
			final JSONObject firstOr = orConditions.getJSONObject(0);
			final JSONObject secondOr = orConditions.getJSONObject(1);
			return or(buildWhereClause(firstOr), //
					buildWhereClause(secondOr), //
					createOptionalWhereClauses(orConditions));
		}
		throw new IllegalArgumentException("The filter is malformed");
	}

	/**
	 * NOTE: @parameter values is always an array of strings
	 */
	private WhereClause buildSimpleWhereClause(final QueryAliasAttribute attribute, final String operator,
			final JSONArray values) throws JSONException {
		final CMAttributeType<?> type = entryType.getAttribute(attribute.getName()).getType();
		if (operator.equals(FilterOperator.EQUAL.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, eq(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.NOT_EQUAL.toString())) {
			Validate.isTrue(values.length() == 1);
			return or(not(condition(attribute, eq(type.convertValue(values.getString(0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.NULL.toString())) {
			Validate.isTrue(values.length() == 0);
			return condition(attribute, isNull());
		} else if (operator.equals(FilterOperator.NOT_NULL.toString())) {
			Validate.isTrue(values.length() == 0);
			return not(condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.GREATER_THAN.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, gt(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.LESS_THAN.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, lt(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.BETWEEN.toString())) {
			Validate.isTrue(values.length() == 2);
			return and(condition(attribute, gt(type.convertValue(values.getString(0)))),
					condition(attribute, lt(type.convertValue(values.getString(1)))));
		} else if (operator.equals(FilterOperator.LIKE.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.CONTAIN.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, contains(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.NOT_CONTAIN.toString())) {
			Validate.isTrue(values.length() == 1);
			return or(not(condition(attribute, contains(type.convertValue(values.getString(0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.BEGIN.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, beginsWith(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.NOT_BEGIN.toString())) {
			Validate.isTrue(values.length() == 1);
			return or(not(condition(attribute, beginsWith(type.convertValue(values.getString(0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.END.toString())) {
			Validate.isTrue(values.length() == 1);
			return condition(attribute, endsWith(type.convertValue(values.getString(0))));
		} else if (operator.equals(FilterOperator.NOT_END.toString())) {
			Validate.isTrue(values.length() == 1);
			return or(not(condition(attribute, endsWith(type.convertValue(values.getString(0))))),
					condition(attribute, isNull()));
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
