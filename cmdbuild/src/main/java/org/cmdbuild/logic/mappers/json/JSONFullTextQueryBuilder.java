package org.cmdbuild.logic.mappers.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.where.EmptyWhereClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.logic.mappers.WhereClauseBuilder;

import com.google.common.collect.Lists;

/**
 * Creates a WhereClause starting from a full text query filter. This means that
 * it searches if the text is in almost one of all the attributes of the
 * specified class
 */
public class JSONFullTextQueryBuilder implements WhereClauseBuilder {

	private final String fullTextQuery;
	private final CMEntryType entryType;

	public JSONFullTextQueryBuilder(final String fullTextQuery, final CMEntryType entryType) {
		Validate.notNull(fullTextQuery);
		Validate.notNull(entryType);
		this.fullTextQuery = fullTextQuery;
		this.entryType = entryType;
	}

	@Override
	public WhereClause build() {
		final List<WhereClause> whereClauses = Lists.newArrayList();
		for (final CMAttribute attribute : entryType.getAttributes()) {
			final WhereClause simpleWhereClause = condition(attribute(entryType, attribute.getName()),
					contains(fullTextQuery));
			whereClauses.add(simpleWhereClause);
		}
		final WhereClause[] whereClausesArray = whereClauses.toArray(new WhereClause[whereClauses.size()]);
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
