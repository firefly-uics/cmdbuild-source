package org.cmdbuild.dao.query;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entry.CMRelation;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.clause.Alias;
import org.cmdbuild.dao.query.clause.ClassAlias;
import org.cmdbuild.dao.query.clause.DomainAlias;

/*
 * Mutable classes used by the driver implementations
 */
public class DBQueryRow implements CMQueryRow {

	Map<CMClass, CMCard> items;

	// Should we have a reference to the QuerySpecs?
	public DBQueryRow() {
		items = new HashMap<CMClass, CMCard>();
	}
	
	public void setCard(final CMClass type, final CMCard card) {
		items.put(type, card);
	}

	@Override
	public CMEntry getEntry(final Alias alias) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CMCard getCard(final ClassAlias alias) {
		throw new UnsupportedOperationException("Not implemented");
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return items.get(type);
	}

	@Override
	public CMRelation getRelation(final DomainAlias alias) {
		throw new UnsupportedOperationException("Not implemented");
	}

}
