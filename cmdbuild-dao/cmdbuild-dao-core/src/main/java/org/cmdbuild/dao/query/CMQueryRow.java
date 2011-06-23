package org.cmdbuild.dao.query;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;

/*
 * Immutable interface to mask result object building
 */
public interface CMQueryRow {

	public CMCard getCard(final Alias alias);
	public CMCard getCard(final CMClass type);

	public QueryRelation getRelation(final Alias alias);
	public QueryRelation getRelation(final CMDomain type);
}
