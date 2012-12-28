package org.cmdbuild.logic;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.*;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

/**
 * Creates a WhereClause starting from a full text query filter. This means that
 * it searches if the text is in almost one of all the attributes of the
 * specified class
 */
public class JSONFullTextQueryBuilder implements WhereClauseBuilder {

	private String fullTextQuery;
	private CMEntryType entryType;

	public JSONFullTextQueryBuilder(String fullTextQuery, CMEntryType entryType) {
		Validate.notNull(fullTextQuery);
		Validate.notNull(entryType);
		this.fullTextQuery = fullTextQuery;
		this.entryType = entryType;
	}

	@Override
	public WhereClause build() {
		List<WhereClause> whereClauses = Lists.newArrayList();
		for (CMAttribute attribute : entryType.getAttributes()) {
			WhereClause simpleWhereClause = condition(attribute(entryType, attribute.getName()),
					contains(fullTextQuery));
			whereClauses.add(simpleWhereClause);
		}
		WhereClause[] whereClausesArray = whereClauses.toArray(new WhereClause[whereClauses.size()]);
		if (whereClauses.isEmpty()) {
			return new EmptyWhereClause();
		}
		if (whereClauses.size() == 1) {
			return whereClauses.get(0);
		} else if (whereClauses.size() == 2) {
			return or(whereClausesArray[0], whereClausesArray[1]);
		} else {
			return or(whereClausesArray[0], whereClausesArray[1],
					Arrays.copyOfRange(whereClausesArray, 2, whereClausesArray.length - 1));
		}
	}

}
