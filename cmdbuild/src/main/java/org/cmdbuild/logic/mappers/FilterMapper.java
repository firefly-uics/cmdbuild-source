package org.cmdbuild.logic.mappers;

import org.cmdbuild.dao.query.clause.where.WhereClause;

public interface FilterMapper {

	public WhereClause deserialize();

}
