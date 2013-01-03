package org.cmdbuild.logic.mappers;

import java.util.List;

import org.cmdbuild.dao.query.clause.OrderByClause;

public interface SorterMapper {

	/**
	 * Method that returns a list of OrderByClause starting from a given
	 * "configuration" (e.g. JSON, XML ecc)
	 */
	public List<OrderByClause> deserialize();

}
