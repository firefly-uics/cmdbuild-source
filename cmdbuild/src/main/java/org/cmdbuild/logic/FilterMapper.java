package org.cmdbuild.logic;

import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface FilterMapper {
	
	public WhereClause deserialize();
	
}
