package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface QuerySpecs {

	CMEntryType getFromType();
	Alias getFromAlias();
	List<JoinClause> getJoins();
	List<OrderByClause> getOrderByClauses();
	Iterable<QueryAliasAttribute> getAttributes();
	WhereClause getWhereClause();
	Long getOffset();
	Long getLimit();
}
