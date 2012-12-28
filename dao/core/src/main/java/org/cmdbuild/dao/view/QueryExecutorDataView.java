package org.cmdbuild.dao.view;

import org.cmdbuild.dao.query.CMQueryResult;
import org.cmdbuild.dao.query.DBQueryResult;
import org.cmdbuild.dao.query.EmptyQuerySpecs;
import org.cmdbuild.dao.query.QuerySpecs;
import org.cmdbuild.dao.query.QuerySpecsBuilder;

public abstract class QueryExecutorDataView implements CMDataView {

	@Override
	public final QuerySpecsBuilder select(final Object... attrDef) {
		return new QuerySpecsBuilder(this) //
				.select(attrDef);
	}

	public final CMQueryResult executeQuery(final QuerySpecsBuilder querySpecsBuilder) {
		return executeNonEmptyQuery(querySpecsBuilder.build());
	}

	public final CMQueryResult executeQuery(final QuerySpecs querySpecs) {
		if (querySpecs instanceof EmptyQuerySpecs) {
			return new DBQueryResult();
		} else {
			return executeNonEmptyQuery(querySpecs);
		}
	}

	/**
	 * Executes a non-empty query returning its result
	 * 
	 * Note: the {@link QuerySpecs} object can be created only by the
	 * {@link QuerySpecsBuilder}, so it is safe to assume that it will not be
	 * invoked on a view different from the one it was created for.
	 * 
	 * @param querySpecs
	 * @return the query result
	 */
	public abstract CMQueryResult executeNonEmptyQuery(QuerySpecs querySpecs);

}
