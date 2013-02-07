package org.cmdbuild.dao.query.clause.from;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMFunctionCall;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class FunctionFromClause implements FromClause {

	private final CMEntryType entryType;
	private final Alias alias;

	public FunctionFromClause(final CMEntryType entryType, final Alias alias) {
		Validate.isTrue(entryType instanceof CMFunctionCall, "from clause must be for function calls only");
		this.entryType = entryType;
		this.alias = alias;
	}

	@Override
	public CMEntryType getType() {
		return entryType;
	}

	@Override
	public Alias getAlias() {
		return alias;
	}

	@Override
	public boolean isHistory() {
		return false;
	}

}
