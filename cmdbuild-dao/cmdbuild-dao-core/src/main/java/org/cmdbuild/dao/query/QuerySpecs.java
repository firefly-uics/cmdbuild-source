package org.cmdbuild.dao.query;

import java.util.List;

import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.ClassAlias;
import org.cmdbuild.dao.query.clause.join.JoinClause;
import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface QuerySpecs {

	ClassAlias getDBFrom();
	List<JoinClause> getJoins();
	Iterable<QueryAliasAttribute> getAttributes();
	WhereClause getWhereClause();
	Long getOffset();
	Long getLimit();
}
