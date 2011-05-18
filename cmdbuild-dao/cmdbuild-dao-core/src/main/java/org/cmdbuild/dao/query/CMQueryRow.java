package org.cmdbuild.dao.query;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.Alias;
import org.cmdbuild.dao.query.clause.ClassAlias;
import org.cmdbuild.dao.query.clause.DomainAlias;

/*
 * Immutable interface to mask result object building
 */
public interface CMQueryRow {

	public CMEntry getEntry(final Alias alias);
	public CMCard getCard(final ClassAlias alias);
	public CMCard getCard(final CMClass type);
	public CMRelation getRelation(final DomainAlias alias);
}
