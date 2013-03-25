package org.cmdbuild.dao.query.clause.from;

import org.apache.commons.lang.Validate;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.ClassHistory;
import org.cmdbuild.dao.query.clause.alias.Alias;

public class ClassFromClause implements FromClause {

	private final CMEntryType entryType;
	private final Alias alias;

	public ClassFromClause(final CMEntryType entryType, final Alias alias) {
		Validate.isTrue(entryType instanceof CMClass, "from clause must be for classes only");
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
		return entryType instanceof ClassHistory;
	}

}
