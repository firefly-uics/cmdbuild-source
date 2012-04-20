package org.cmdbuild.dao.query;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entry.DBCard;
import org.cmdbuild.dao.entry.DBEntry;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.query.clause.QueryRelation;
import org.cmdbuild.dao.query.clause.alias.Alias;

/*
 * Note: Mutable classes because it is supposed to be used by driver
 *       implementations.
 * 
 * TODO Consider named attributes not bounded to a class or a domain to
 *      allow functions or aliases for other attributes
 */
public class DBQueryRow implements CMQueryRow {

	Map<Alias, DBCard> cards;
	Map<Alias, QueryRelation> relations;

	// Should we have a reference to the QuerySpecs?
	public DBQueryRow() {
		cards = new HashMap<Alias, DBCard>();
		relations = new HashMap<Alias, QueryRelation>();
	}

	public void setCard(final Alias alias, final DBCard card) {
		cards.put(alias, card);
	}

	public void setRelation(final Alias alias, final QueryRelation relation) {
		relations.put(alias, relation);
	}

	public void setValue(final Alias alias, final String key, final Object value) {
		getEntry(alias).setOnly(key, value);
	}

	private DBEntry getEntry(final Alias alias) {
		if (cards.containsKey(alias)) {
			return cards.get(alias);
		} else if (relations.containsKey(alias)) {
			return relations.get(alias).getRelation();
		} else {
			throw new IllegalArgumentException("No alias " + alias);
		}
	}

	@Override
	public CMCard getCard(final CMClass type) {
		return getCard(Alias.canonicalAlias(type));
	}

	@Override
	public CMCard getCard(final Alias alias) {
		if (cards.containsKey(alias)) {
			return cards.get(alias);
		} else {
			throw new IllegalArgumentException("No alias " + alias);
		}
	}

	@Override
	public QueryRelation getRelation(final Alias alias) {
		if (relations.containsKey(alias)) {
			return relations.get(alias);
		} else {
			throw new IllegalArgumentException("No alias " + alias);
		}
	}

	@Override
	public QueryRelation getRelation(CMDomain type) {
		return getRelation(Alias.canonicalAlias(type));
	}
}
