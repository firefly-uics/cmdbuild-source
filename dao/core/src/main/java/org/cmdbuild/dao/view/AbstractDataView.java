package org.cmdbuild.dao.view;

import org.cmdbuild.dao.query.QuerySpecsBuilder;

public abstract class AbstractDataView implements CMDataView {

	@Override
	public final QuerySpecsBuilder select(final Object... attrDef) {
		return new QuerySpecsBuilder(viewForBuilder(), viewForRunner()) //
				.select(attrDef);
	}

	protected CMDataView viewForBuilder() {
		return this;
	}

	protected CMDataView viewForRunner() {
		return this;
	}

}
