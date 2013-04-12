package org.cmdbuild.dao.query.clause.from;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;

public interface FromClause {

	CMEntryType getType();

	Alias getAlias();

	boolean isHistory();

}
